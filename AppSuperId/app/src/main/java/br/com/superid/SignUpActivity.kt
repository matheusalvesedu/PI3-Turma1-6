package br.com.superid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewSignUp()
        }
    }
}

fun saveNewAccount(name: String, email: String, password: String) {
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
                        Log.e("Firesore", "Erro ao salvar a conta", e)
                    }

                Log.i("CREATION-TEST", "ID do novo usuário: ${user.uid}")
            } else {
                Log.i("CREATION-TEST", "Usuário não criado.")
                task.exception?.let { e ->
                    Log.e("CREATION-ERROR", "Erro ao criar usuário", e)
                }
            }
        }
}

fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

@Preview
@Composable
fun PreviewSignUp(){
    SignUp()
}

@Composable
fun SignUp(){

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Cadastro",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .padding(10.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {name = it},
            label = { Text("Digite seu Nome")},
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Digite seu E-mail")},
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("Digite sua Senha")},
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Button(
            onClick = {
                saveNewAccount(name,email, password)
            },
            enabled = email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty(),
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = "Salvar", fontSize = 24.sp)
        }
    }
}