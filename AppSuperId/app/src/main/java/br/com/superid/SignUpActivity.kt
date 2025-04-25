package br.com.superid

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                PreviewSignUp()
            }
        }
    }
}

data class PasswordRequirements(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false
)

fun checkPasswordRequirements(password: String): PasswordRequirements{
    return PasswordRequirements(
        hasMinLength = password.length >= 8,
        hasUppercase = password.any {it.isUpperCase()},
        hasLowerCase = password.any {it.isLowerCase()},
        hasDigit = password.any {it.isDigit()},
        hasSpecialChar = password.any {it in "@#$%&+=!"}
    )
}

fun saveNewAccount(name: String, email: String, password: String, context: Context) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if(task.isSuccessful){
                val user = auth.currentUser
                val userAccount = hashMapOf(
                    "name" to name,
                    "email" to email
                )
                db.collection("accounts").document(user!!.uid).set(userAccount)
                    .addOnSuccessListener{
                        Log.d("Firestore", "Conta salva com sucesso!")
                    }
                    .addOnFailureListener{ e ->
                        Log.e("Firestore", "Erro ao salvar a conta", e)
                    }
                Toast.makeText(context, "Cadastro realizado!", Toast.LENGTH_SHORT).show()
                mudarTela(context, LoginActivity::class.java)

                Log.i("CREATION-TEST", "ID do novo usuário: ${user.uid}")
            } else {
                Log.i("CREATION-TEST", "Usuário não criado.")
                task.exception?.let { e ->
                    Log.e("CREATION-ERROR", "Erro ao criar usuário", e)
                }
                Toast.makeText(context, "Erro: Cadastro não realizado", Toast.LENGTH_LONG).show()
            }
        }
}

@Preview
@Composable
fun PreviewSignUp(){
    SignUpFlow()
}

@Composable
fun SignUpFlow(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "name") {
        composable("name") { NameScreen(navController) }
        composable("email/{name}") { entry ->
            entry.arguments?.getString("name")?.let { name ->
                EmailScreen(navController,name)
            }
        }
        composable("password/{name}/{email}") { entry->
            entry.arguments?.getString("name")?.let { name ->
                entry.arguments?.getString("email")?.let{email ->
                    PasswordScreen(navController,name,email)
                }
            }

        }
    }

}

@Composable
fun NameScreen(navController: NavController){

    var name by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AppColors.white,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ){
                Button(
                    onClick = { navController.navigate("email/$name") },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (name.isNotBlank()) AppColors.gunmetal else AppColors.jet,
                        contentColor = AppColors.white
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)

                ) {
                    Text(text = "Avançar",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 12.sp,
                        color = if(name.isNotBlank()) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
                .padding((innerPadding)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(text = "Boas vindas ao Super ID!\nDigite seu nome completo:",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(name, { newName -> name = newName }, "Digite seu nome completo")

        }
    }
}

@Composable
fun EmailScreen(navController: NavController, name: String) {

    var email by remember { mutableStateOf("") }

    //Valores que validam senha e email
    val isEmailValid = remember(email){
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        containerColor = AppColors.white,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = { navController.navigate("password/$name/$email") },
                    enabled = email.isNotBlank() && isEmailValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (email.isNotBlank() && isEmailValid) AppColors.gunmetal else AppColors.jet,
                        contentColor = AppColors.white
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)

                ) {
                    Text(
                        text = "Avançar",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 12.sp,
                        color = if (email.isNotBlank() && isEmailValid) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
                .padding((innerPadding)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Agora, digite seu e-mail:",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(email, { newEmail -> email = newEmail }, "Digite seu e-mail")

        }
    }
}

@Composable
fun PasswordScreen(navController: NavController, name: String, email: String) {

    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val passwordRequirements = checkPasswordRequirements(password)
    val context = LocalContext.current

    val isPasswordValid = passwordRequirements.hasDigit &&
            passwordRequirements.hasUppercase &&
            passwordRequirements.hasLowerCase &&
            passwordRequirements.hasSpecialChar &&
            passwordRequirements.hasMinLength


    Scaffold(
        containerColor = AppColors.white,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = { saveNewAccount(name,email,password,context) },
                    enabled = password.isNotBlank() && isPasswordValid && password == passwordConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (password.isNotBlank() && isPasswordValid && password == passwordConfirm) AppColors.gunmetal else AppColors.jet,
                        contentColor = AppColors.white
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)

                ) {
                    Text(
                        text = "Criar conta",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 12.sp,
                        color = if (password.isNotBlank() && isPasswordValid && password == passwordConfirm) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
                .padding((innerPadding)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Agora, digite sua senha:",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(password, { newPassword -> password = newPassword }, "Digite sua senha")

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = "Confirme sua senha:",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(passwordConfirm, { newPasswordConfirm -> passwordConfirm = newPasswordConfirm }, "Confirme sua senha")

            if(password.isNotBlank()){
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RequirementItem("8 caracteres",passwordRequirements.hasMinLength)
                    RequirementItem("1 letra maiúscula",passwordRequirements.hasUppercase)
                    RequirementItem("1 letra minúscula",passwordRequirements.hasLowerCase)
                    RequirementItem("1 número",passwordRequirements.hasDigit)
                    RequirementItem("1 caractere especial @#$%&+=!",passwordRequirements.hasSpecialChar)
                }
            }
        }
    }
}

//Função composable que gera os requerimentos da senha
@Composable
fun RequirementItem(text: String, isChecked: Boolean){
    Row(verticalAlignment = Alignment.CenterVertically){
        Checkbox(
            checked = isChecked,
            enabled = false,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.gunmetal,
                uncheckedColor = AppColors.jet
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isChecked) AppColors.gunmetal else AppColors.jet,
            fontFamily = PoppinsFonts.regular
        )
    }
}
