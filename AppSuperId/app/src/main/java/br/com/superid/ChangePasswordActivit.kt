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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.compose.rememberNavController
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

class PasswordRecoveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                PasswordRecoveryScreen(rememberNavController())
            }
        }
    }
}

// Função para enviar o e-mail de recuperação de senha
fun sendPasswordResetEmail(
    email: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth = Firebase.auth

    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("PasswordRecovery", "E-mail de recuperação enviado com sucesso para $email")
                onSuccess()
            } else {
                val exception = task.exception
                if (exception != null) {
                    Log.e("PasswordRecovery", "Erro ao enviar e-mail de recuperação", exception)
                    onFailure(exception)
                }
            }
        }
}

@Preview
@Composable
fun PasswordRecoveryPreview() {
    SuperIDTheme {
        PasswordRecoveryScreen(rememberNavController())
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    // Validação do e-mail
    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // titulo
    val titleText = if (emailSent) {
        "E-mail de recuperação enviado!"
    } else {
        "Digite seu e-mail usado na criação da conta:"
    }

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            activityBackButton(activity)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
                .padding(innerPadding),
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
                text = titleText,
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.size(30.dp))

            if (emailSent) {
                Image(
                    painter = painterResource(id = R.drawable.logo_circled_envelope),
                    contentDescription = "Imagem envelope circular",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(150.dp) // ajuste o tamanho se quiser
                )

                Spacer(modifier = Modifier.size(30.dp))

                Text(
                    text= "Verifique sua caixa de entrada!",
                    fontFamily = PoppinsFonts.medium,
                    fontSize = 24.sp,
                    color = AppColors.gunmetal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                    .padding(horizontal = 20.dp)
                )

            }

            if (!emailSent) {
                inputBox(email, { newEmail -> email = newEmail }, "Digite seu e-mail")

                Spacer(modifier = Modifier.size(40.dp))

                Button(
                    onClick = {
                        isLoading = true
                        sendPasswordResetEmail(
                            email,
                            context,
                            onSuccess = {
                                isLoading = false
                                emailSent = true
                            },
                            onFailure = { e ->
                                isLoading = false
                                val errorMessage = when {
                                    e.localizedMessage?.contains("no user record") == true -> {
                                        "E-mail não encontrado. Verifique e tente novamente."
                                    }

                                    else -> {
                                        "Erro: ${e.localizedMessage}"
                                    }
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = email.isNotBlank() && isEmailValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (email.isNotBlank() && isEmailValid) AppColors.gunmetal else AppColors.jet,
                        contentColor = AppColors.white
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = AppColors.satinSheenGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Enviar",
                            fontFamily = PoppinsFonts.medium,
                            fontSize = 16.sp,
                            color = if (email.isNotBlank() && isEmailValid) AppColors.platinum else AppColors.gunmetal
                        )
                    }
                }
            }
        }
    }
}




