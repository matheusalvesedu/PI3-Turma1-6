package br.com.superid

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import br.com.superid.ui.theme.AppColors
import androidx.compose.ui.text.input.PasswordVisualTransformation

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginPreview()
                }
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
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task->
        if (task.isSuccessful){
            Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_SHORT).show()
            Log.i("AUTH-TESTE", "LOGIN REALIZADO"+
                    "UID: ${task.result.user!!.uid}")

            onSuccess()
            mudarTela(context, PrincipalScreenActivity::class.java)
        }else{
            Toast.makeText(context, "Erro: Login não realizado", Toast.LENGTH_LONG).show()
            Log.i("AUTH-TESTE","Login não realizado")

            onFailure(task.exception ?: Exception("Erro desconhecido"))
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
        containerColor = MaterialTheme.colorScheme.background,
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(16.dp))

            inputBox(email, { email = it }, "Digite seu e-mail")

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Digite sua senha",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
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
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    containerColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            loginError?.let {
                Text(
                    "Email ou senha incorretos",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Esqueci minha senha",
                color = Color.Blue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable {
                    mudarTela(context, PasswordRecoveryActivity::class.java)
                }
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
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Entrar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


