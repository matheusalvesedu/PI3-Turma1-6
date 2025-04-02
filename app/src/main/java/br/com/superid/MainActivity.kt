package br.com.superid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SuperIDApp()
            }
        }
    }
}

@Preview
@Composable
fun SuperIDApp(){
    SuperID(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun SuperID(modifier: Modifier = Modifier){
    var email by remember { mutableStateOf("")}
    var senha by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequesterSenha = remember { FocusRequester() }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "SuperID",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(10.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Endereço de email") },
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesterSenha.requestFocus() }
            )
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha") },
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp)
                .focusRequester(focusRequesterSenha),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                saveNewAccount(email, senha)
                email = ""
                senha = ""
            },
            enabled = email.isNotEmpty() && senha.isNotEmpty()
        ) {
            Text(text = "Salvar", fontSize = 24.sp)
        }
    }
}
//Criptografar a senha
fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

/*Função que verifica senha no login
fun checkPassword(inputPassword: String, hashedPassword: String): Boolean {
    return BCrypt.checkpw(inputPassword, hashedPassword)
}*/

/***
 * Função que adiciona uma conta no Firestore.
 */

fun saveNewAccount(email: String, senha: String) {
    // 1° passo: Obtendo a instância db (singleton)
    val db = Firebase.firestore
    val hashedSenha = hashPassword(senha) // Criptografa a senha

    val newAccount = hashMapOf(
        "email" to email,
        "senha" to hashedSenha  // Salva apenas o hash!
    )

    db.collection("accounts").add(newAccount)
        .addOnSuccessListener {
            Log.d("Firestore", "Conta salva com sucesso!")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Erro ao salvar conta", e)
        }
}


