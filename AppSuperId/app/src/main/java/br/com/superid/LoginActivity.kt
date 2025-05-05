package br.com.superid

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import br.com.superid.ui.theme.AppColors
import androidx.compose.ui.text.input.PasswordVisualTransformation

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                LoginPreview()
            }
        }
    }
}

fun loginUser(
    email: String,
    password: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener{ task->
        if (task.isSuccessful){
            Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_SHORT).show()
            Log.i("AUTH-TESTE", "LOGIN REALIZADO"+
                    "UID: ${task.result.user!!.uid}")

            mudarTela(context, PrincipalScreenActivity::class.java)

        }else{
            Toast.makeText(context, "Erro: Login não realizado", Toast.LENGTH_LONG).show()
            Log.i("AUTH-TESTE","Login não realizado")
        }
    }
}

@Preview
@Composable
fun LoginPreview(){
    LoginScreen(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember {mutableStateOf(false)}
    var activity = LocalActivity.current
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            activityBackButton(activity)
        }
    )
    { innerPadding ->
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
            Spacer(modifier = Modifier.height(75.dp))
            Icon(
                painter = painterResource(R.drawable.logo_superid_darkblue),
                contentDescription = "Logo do Super ID",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Acesse sua conta",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,)

            Spacer(modifier = Modifier.height(16.dp))

            inputBox(email, { email = it }, "Digite seu e-mail")

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Digite sua senha",
                        fontSize = 12.sp,
                        fontFamily = PoppinsFonts.regular
                    )
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) {
                        painterResource(id = R.drawable.ic_visibility_on)
                    } else {
                        painterResource(id = R.drawable.ic_visibility_off)
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = icon,
                            contentDescription = "Senha visivel",
                            modifier = Modifier.size(24.dp))
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = AppColors.gunmetal,
                    unfocusedIndicatorColor = AppColors.platinum,
                    containerColor = Color.Transparent,
                    focusedLabelColor = AppColors.gunmetal,
                    cursorColor = AppColors.gunmetal
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            loginError?.let {
                Text(
                    "Email ou senha incorretos",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Esqueci minha senha",
                color = Color.Blue,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    mudarTela(context, InitialPageActivity::class.java)
                },
                fontFamily = PoppinsFonts.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    loginUser(
                        email = email,
                        password = password,
                        context = context,
                        onSuccess = {
                            isLoading = false
                            mudarTela(context, InitialPageActivity::class.java)
                            Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { exception ->
                            isLoading = false
                            loginError = "Erro: ${exception.message}"
                        }
                    )
                },
                enabled = email.isNotBlank() && password.isNotBlank() && isEmailValid,
                modifier = Modifier
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (email.isNotBlank() && password.isNotBlank() && isEmailValid) AppColors.gunmetal else AppColors.jet
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.white,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 16.sp,
                        color = AppColors.platinum
                    )
                }
            }
        }
        Text(
            text = "Esqueci minha senha",
            fontFamily = PoppinsFonts.regular,
            fontSize = 16.sp,
            color = AppColors.gunmetal,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable {
                    mudarTela(context, PasswordRecoveryActivity::class.java )
                }
        )

    }
}


