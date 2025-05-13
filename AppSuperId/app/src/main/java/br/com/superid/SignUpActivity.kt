package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import kotlinx.coroutines.delay

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
    val UID = user!!.uid

    val userAccount = hashMapOf(
        "name" to name,
        "email" to email,
        "firstAccess" to true
    )

    val defaultCategories = listOf(
        hashMapOf(
            "Nome" to "Sites Web",
            "Cor" to "0xFFCCA43B"
        ),
        hashMapOf(
            "Nome" to "Aplicativos",
            "Cor" to "0xFF3BBDCC"
        ),
        hashMapOf(
            "Nome" to "Teclados de Acesso Físico",
            "Cor" to "0xFF58CC3B"
        )
    )


    db.collection("accounts").document(UID).set(userAccount)
        .addOnSuccessListener{

            for (categoria in defaultCategories) {

                val categoryName = categoria["Nome"] ?: "Categoria sem nome"

                db.collection("accounts").document(UID).collection("Categorias")
                    .add(categoria)

            }

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

fun createUser(
    name: String,
    email: String,
    password: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {

    val auth = Firebase.auth

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if(task.isSuccessful){

                val user = auth.currentUser

                saveNewAccountToDB(user,name,email)
                sendEmailVerification(user,context)
                onSuccess()

                Log.i("CREATION-TEST", "Usuario criado com sucesso UID -> ${user?.uid} ")
            } else {

                onFailure()

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
    SuperIDTheme {
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
}

@Composable
fun CheckBoxTermosUso(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
){

    var showPopUp by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        )

        Text(
            text = "Ao prosseguir você concorda com os termos de uso do app.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { showPopUp = true }
        )

        if(showPopUp){
            AlertDialog(
                onDismissRequest = {showPopUp = false},
                title = { Text("Termos de Uso") },
                text = { Text("Bem-vindo ao SuperID!\n" +
                        "\n" +
                        "Este aplicativo foi desenvolvido com fins educacionais no âmbito do Projeto Integrador 3 da PUC-Campinas. Ao utilizar o SuperID, você concorda com os seguintes termos:\n" +
                        "\n" +
                        "1. O SuperID é um gerenciador de autenticações que permite a criação de contas, armazenamento seguro de senhas e login sem senha.\n" +
                        "2. Seus dados (nome, e-mail, UID do dispositivo) são armazenados no Firebase de forma segura e com fins acadêmicos.\n" +
                        "3. As senhas cadastradas são criptografadas e associadas a tokens únicos.\n" +
                        "4. O aplicativo pode ser usado para login em sites parceiros, utilizando QR Code e autenticação segura.\n" +
                        "5. A redefinição da senha mestre depende da validação do seu e-mail.\n" +
                        "6. Este app não atende a padrões avançados de segurança da informação e não deve ser usado em ambientes reais ou com dados sensíveis fora do contexto educacional.\n" +
                        "7. Ao criar sua conta, você declara estar ciente de que o uso do app é exclusivamente acadêmico e que seus dados podem ser apagados ao fim do semestre letivo.\n" +
                        "\n" +
                        "Para mais informações, entre em contato com a equipe de desenvolvimento ou os professores responsáveis pelo projeto.\n" +
                        "\n" +
                        "PUC-Campinas - Engenharia de Software") },
                confirmButton = {
                    TextButton(onClick = { showPopUp = false }) {
                        Text("Fechar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.background(color = AppColors.white)
            )
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
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(6.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun NameScreen(navController: NavController){

    var name by remember { mutableStateOf("") }
    val activity = LocalContext.current as? Activity

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        containerColor = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)

                ) {
                    Text(text = "Avançar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp
                        ),
                        color = if (name.isNotBlank())
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenBackButton(navController, context)
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
                        containerColor = if (email.isNotBlank() && isEmailValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)

                ) {
                    Text(
                        text = "Avançar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp
                        ),
                        color = if (email.isNotBlank() && isEmailValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
                ),
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
    var termosAceitos by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }

    // Delay visual na criação de conta
    var shouldNavigate by remember { mutableStateOf(false) }
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            kotlinx.coroutines.delay(1000)
            navController.navigate("verification/$name/$email")
        }
    }

    val isPasswordValid = passwordRequirements.hasDigit &&
            passwordRequirements.hasUppercase &&
            passwordRequirements.hasLowerCase &&
            passwordRequirements.hasSpecialChar &&
            passwordRequirements.hasMinLength

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenBackButton(navController, context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            passwordInputBox(password, { newPassword -> password = newPassword }, "Digite sua senha")

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

            Spacer(modifier = Modifier.size(16.dp))

            CheckBoxTermosUso(
                termosAceitos,
                onCheckedChange = {termosAceitos = it}
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isLoading = true
                    createUser(
                        name,
                        email,
                        password,
                        context,
                        onSuccess = {shouldNavigate = true},
                        onFailure = {
                            isLoading = false
                            Toast.makeText(context, "Erro ao criar conta\nTente Novamente.", Toast.LENGTH_LONG).show()
                            navController.navigate("name")
                        }
                    )

                },
                enabled = password.isNotBlank() && isPasswordValid && password == passwordConfirm && termosAceitos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (password.isNotBlank() &&
                        isPasswordValid &&
                        !shouldNavigate &&
                        password == passwordConfirm &&
                        termosAceitos) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(50.dp)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Criar conta",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (password.isNotBlank() &&
                            isPasswordValid &&
                            !shouldNavigate &&
                            password == passwordConfirm &&
                            termosAceitos) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationScreen(navController: NavController, name: String, email: String){

    var isVerified by remember { mutableStateOf(false) }
    val auth = Firebase.auth
    var context = LocalContext.current

    LaunchedEffect(true) {
        while(!isVerified){

            val user = auth.currentUser

            user?.reload()
            if(user?.isEmailVerified == true){
                isVerified = true
                delay(1500)
                mudarTela(context, PrincipalScreenActivity::class.java)
            }

            delay(3000)

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
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
            text = "Verificação de endereço de e-mail",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
        )

        if(!isVerified){
            Text(
                text = "Aguardando a verificação do e-mail...",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(25.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(50.dp)
            )

        }else{

            Text(
                text = "E-mail verificado!!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(25.dp))

            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = "Verificado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )

        }

    }
}