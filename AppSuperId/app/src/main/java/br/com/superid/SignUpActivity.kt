package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.github.cdimascio.dotenv.dotenv
import org.checkerframework.checker.units.qual.C
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


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

fun saveNewAccountToDB(user: FirebaseUser?, name: String, email: String){

    val db = Firebase.firestore

    val userAccount = hashMapOf(
        "name" to name,
        "email" to email,
        "firstAccess" to "true"
    )

    db.collection("accounts").document(user!!.uid).set(userAccount)
        .addOnSuccessListener{
            Log.d("Firestore", "Informações da conta salva com sucesso!")
        }
        .addOnFailureListener{ e ->
            Log.e("Firestore", "Erro ao salvar as Informações da conta", e)
        }

}

fun sendEmailVerification(user: FirebaseUser?, context: Context){

    user?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                Log.i( "EmailVerification","Email de verificação enviado com sucesso!! ")
            }else{
                Log.i("EmailVerification", "Email de verificação falhou ao ser enviado -> ${task.exception} ")
            }
        }

}

fun createUser(name: String, email: String, password: String, context: Context) {

    val auth = Firebase.auth

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if(task.isSuccessful){

                val user = auth.currentUser

                saveNewAccountToDB(user,name,email)
                sendEmailVerification(user,context)
                Log.i("CREATION-TEST", "Usuario criado com sucesso UID -> ${user?.uid} ")
            } else {
                Log.i("CREATION-TEST", "Usuário não criado.")
                task.exception?.let { e ->
                    Log.e("CREATION-ERROR", "Erro ao criar usuário", e)
                }
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
                entry.arguments?.getString("email")?.let{ email ->
                    PasswordScreen(navController,name,email)
                }
            }

        }

        composable("verification/{name}/{email}"){ entry ->
            entry.arguments?.getString("name")?.let { name->
                entry.arguments?.getString("email")?.let{ email ->
                    VerificationScreen(navController,name,email)
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

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 8.sp,
            color = if (isChecked) AppColors.gunmetal else AppColors.jet,
            fontFamily = PoppinsFonts.regular
        )
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun NameScreen(navController: NavController){

    var name by remember { mutableStateOf("") }
    var activity = LocalContext.current as? Activity
    var context = LocalContext.current

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            activityBackButton(activity)
        },
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
                    .fillMaxWidth(),
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
    var context = LocalContext.current

    //Valores que validam senha e email
    val isEmailValid = remember(email){
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            screenBackButton(navController, context)
        },
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
                    .fillMaxWidth(),
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

    var isLoading by remember { mutableStateOf(false) }

    // Delay visual na criação de conta
    var shouldNavigate by remember { mutableStateOf(false) }
    LaunchedEffect(shouldNavigate) {
        if(shouldNavigate){
            kotlinx.coroutines.delay(1500)
            navController.navigate("verification/$name/$email")
        }
    }

    val isPasswordValid = passwordRequirements.hasDigit &&
            passwordRequirements.hasUppercase &&
            passwordRequirements.hasLowerCase &&
            passwordRequirements.hasSpecialChar &&
            passwordRequirements.hasMinLength

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            screenBackButton(navController, context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding((innerPadding)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
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

            passwordInputBox(password, { newPassword -> password = newPassword }, "Digite sua senha")

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

            passwordInputBox(passwordConfirm, { newPasswordConfirm -> passwordConfirm = newPasswordConfirm }, "Confirme sua senha")

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

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    isLoading = true
                    createUser(name,email,password,context)
                    shouldNavigate = true
                },
                enabled = password.isNotBlank() && isPasswordValid && password == passwordConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (password.isNotBlank() &&
                                        isPasswordValid &&
                                        !shouldNavigate &&
                                        password == passwordConfirm) AppColors.gunmetal else AppColors.jet,
                    contentColor = AppColors.white
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(300.dp)
                    .height(80.dp)
                    .padding(bottom = 20.dp)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.white,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Criar conta",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 30.sp,
                        color = if (password.isNotBlank() &&
                                    isPasswordValid &&
                                    !shouldNavigate &&
                                    password == passwordConfirm) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationScreen(navController: NavController, name: String, email: String){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppColors.white),
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
            text = "Verifique sua conta",
            fontFamily = PoppinsFonts.medium,
            fontSize = 24.sp,
            color = AppColors.gunmetal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

    }
}

