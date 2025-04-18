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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logo_superid_black),
                            contentDescription = "Logo do Super ID",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(top = 10.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(color = Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(text = "Login",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Digite seu Email") },
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black
            )
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha") },
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                loginAuth(email,senha,context)
            },
            enabled = email.isNotEmpty() && senha.isNotEmpty(),
            modifier = Modifier.padding(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(text = "Entrar",
                fontSize = 24.sp,
                color = Color.White
            )
        }

    }
 }
}
