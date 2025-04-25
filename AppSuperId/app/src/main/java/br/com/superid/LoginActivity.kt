package br.com.superid

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import br.com.superid.ui.theme.AppColors
import br.com.superid.PoppinsFonts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


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

fun loginAuth(email:String, senha:String,context:Context){
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener{ task->
        if (task.isSuccessful){
            Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_SHORT).show()
            Log.i("AUTH-TESTE", "LOGIN REALIZADO"+
                    "UID: ${task.result.user!!.uid}")

        }else{
            Toast.makeText(context, "Erro: Login não realizado", Toast.LENGTH_LONG).show()
            Log.i("AUTH-TESTE","Login não realizado")
        }
    }
}

@Preview
@Composable
fun LoginPreview(){
    login(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun login(modifier: Modifier = Modifier){
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppColors.white),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
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

        Spacer(modifier = Modifier.height(80.dp))

        Text(text = "Bem-vindo de volta! \nDigite seu e-mail e senha:",
            fontFamily = PoppinsFonts.medium,
            fontSize = 24.sp,
            color = AppColors.gunmetal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
        )

        Spacer(modifier = Modifier.size(10.dp))

        inputBox(email, { newEmail -> email = newEmail }, "Digite seu e-mail")

        inputBox(senha, { newSenha -> senha = newSenha }, "Digite sua senha")

        Spacer(modifier = Modifier.height(180.dp))

        Button(
            onClick = {
                loginAuth(email,senha,context)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.gunmetal
            ),
        ) {
            Text(text = "Entrar",
                fontFamily = PoppinsFonts.medium,
                fontSize = 30.sp,
                color = AppColors.platinum
            )
        }

    }
}
