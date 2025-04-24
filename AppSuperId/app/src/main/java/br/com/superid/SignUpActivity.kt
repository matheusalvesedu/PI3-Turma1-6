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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

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

fun passwordValidation(password: String): Boolean {
    val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!]).{8,}$".toRegex()
    return PASSWORD_REGEX.matches(password)
}

@Preview
@Composable
fun PreviewSignUp(){
    SignUp()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(){
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    //Valores que validam senha e email
    val isEmailValid = remember(email){
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = passwordValidation(password)
    var isButtonEnabled by remember { mutableStateOf(true) }

    //Valor utilizado tanto na rolagem da tela, como no tempo para reabilitar o botão
    val scope = rememberCoroutineScope()

    //Variável para identificar em qual tela/activity está
    var context = LocalContext.current

    //Valores e funções utilizadas para a rolagem da tela ao clicar em uma TextField
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var focusedTextFieldOffset by remember { mutableIntStateOf(0) }
    fun calculateScrollOffset(offsetProvider: () -> Int) {
        scope.launch {
            val target = offsetProvider()
            val visibleArea = scrollState.value + scrollState.viewportSize
            if (target > visibleArea) {
                scrollState.animateScrollTo(target)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .imePadding(),
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
                .verticalScroll(scrollState)
                .background(color = Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Cadastro",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(20.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Digite seu Nome") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
                    .onGloballyPositioned {
                        focusedTextFieldOffset = it.positionInParent().y.toInt()
                    }
                    .onFocusEvent {
                        if(it.isFocused){
                            calculateScrollOffset {
                                focusedTextFieldOffset
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Digite seu E-mail") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .onGloballyPositioned {
                        focusedTextFieldOffset = it.positionInParent().y.toInt()
                    }
                    .onFocusEvent {
                        if(it.isFocused){
                            calculateScrollOffset {
                                focusedTextFieldOffset
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )

            if(!isEmailValid && email.isNotEmpty()){
                Text("Email inválido",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Digite sua Senha") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
                    .onGloballyPositioned {
                        focusedTextFieldOffset = it.positionInParent().y.toInt()
                    }
                    .onFocusEvent {
                        if(it.isFocused){
                            calculateScrollOffset {
                                focusedTextFieldOffset
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )

            if(!isPasswordValid && password.isNotEmpty()){
                Text("A senha deve ter pelo menos 8 caracteres,\numa maiúscula, uma minúscula, um dígito\n e um especial (@#\$%^&+=!).",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                label = { Text("Confirme sua Senha") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .onGloballyPositioned {
                        focusedTextFieldOffset = it.positionInParent().y.toInt()
                    }
                    .onFocusEvent {
                        if(it.isFocused){
                            calculateScrollOffset {
                                focusedTextFieldOffset
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )

            if(password.isNotEmpty() && passwordConfirm.isNotEmpty()){
                if(password != passwordConfirm) {
                    Text("Senhas não coincidem",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    saveNewAccount(name, email, password, context)
                    isButtonEnabled = false
                    scope.launch {
                        delay(5000L)
                        isButtonEnabled = true
                    }
                },
                enabled = name.isNotEmpty() && email.isNotEmpty()
                        && isEmailValid && isPasswordValid
                        && password.isNotEmpty() && passwordConfirm.isNotEmpty()
                        && password == passwordConfirm
                        && isButtonEnabled,
                modifier = Modifier.padding(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(text = "Salvar",
                    fontSize = 24.sp,
                    color = Color.White)
            }
        }
    }
}