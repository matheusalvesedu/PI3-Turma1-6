package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import br.com.superid.ui.theme.onPrimaryContainerLight
import br.com.superid.ui.theme.onPrimaryLight
import br.com.superid.ui.theme.primaryContainerLight
import br.com.superid.ui.theme.primaryLight
import br.com.superid.ui.theme.tertiaryContainerLight
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import java.util.Base64


class CadastroSenhaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                CadastroSenhaScreen()
            }
        }
    }
}

//função que gera um access token aleatorio
fun generateAccessToken() :String {
    val randomBytes = ByteArray(192) //gera um array de bytes com 192 de tamanho pois ao virar base64 ele ira atinger os 256 caracteres requisitados
    SecureRandom().nextBytes(randomBytes) //Secure random é um gerador securo de numeros, next bytes transforma os numeros em  bytes que sao alocados no meu array randomBytes
    return Base64.getEncoder().encodeToString(randomBytes) //transformo o array para base64
}

//função que salva a senha em uma subcoleçao do usuario
fun savePasswordToDb(
    user: FirebaseUser,
    passwordNickName: String,
    login: String?,
    password: String,
    description: String?,
    category: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
){

    val db = Firebase.firestore
    val UID = user.uid

    val encryptedPassword = aesEncryptWithKey(password)
    val accessToken = generateAccessToken()

    val passwordData = hashMapOf(
        "Apelido da senha" to passwordNickName,
        "login" to login,
        "senha" to encryptedPassword,
        "descrição" to description,
        "categoria" to category,
        "accessToken" to accessToken
    )

    db.collection("accounts")
        .document(UID)
        .collection("Senhas")
        .add(passwordData)
        .addOnSuccessListener{

            onSuccess()

            Log.d("Firestore", "Informações da nova senha salvas com sucesso!")
        }
        .addOnFailureListener{ e ->

            onFailure()

            Log.e("Firestore", "Erro ao salvar as Informações da nova senha", e)
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(user: FirebaseUser,context: Context,selectedText: String, onCategorySelected: (String) -> Unit){

    var isExpanded by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    LaunchedEffect(Unit) {
        getCategorias(user.uid, context) { result ->
            categorias = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {isExpanded = !isExpanded}
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)},
                placeholder = {
                    Text(text = "Escolha sua categoria",
                        fontSize = 12.sp,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
                    .menuAnchor(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = primaryContainerLight,
                    unfocusedIndicatorColor = onPrimaryContainerLight,
                    containerColor = Color.Transparent,
                    focusedLabelColor = primaryContainerLight,
                    cursorColor = primaryContainerLight
                )
            )

            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false } ) {
                categorias.forEach{ item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                item.nome,
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {

                            onCategorySelected(item.nome)
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

    }

}

@SuppressLint("ContextCastToActivity")
@Composable
fun CadastroSenhaScreen(){

    val auth = Firebase.auth
    val user = auth.currentUser

    var passwordNickName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            activityBackButton(activity)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text(
                text = "Cadastre uma nova senha:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(20.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            inputBox(passwordNickName,{ newPasswordNickName -> passwordNickName = newPasswordNickName },"Digite um apelido para a senha")

            Spacer(modifier = Modifier.size(12.dp))

            inputBox(login,{ newlogin -> login = newlogin },"Digite seu login (opcional)")

            Spacer(modifier = Modifier.size(12.dp))

            passwordInputBox(password, { newPassword -> password = newPassword }, "Digite sua senha")

            Spacer(modifier = Modifier.size(12.dp))

            inputBox(description,{ newDescription -> description = newDescription},"Digite sua descrição (opcional)")

            Spacer(modifier = Modifier.size(12.dp))

            if(user != null){
                DropDown(user,context,category, onCategorySelected = { newCategory -> category = newCategory})
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isLoading = true
                    if(user != null){
                        savePasswordToDb(
                            user,
                            passwordNickName,
                            login,
                            password,
                            description,
                            category,
                            onSuccess = {
                                Toast.makeText(context, "Nova senha cadastrada com sucesso", Toast.LENGTH_LONG).show()
                                activity?.finish()
                            },
                            onFailure = {  Toast.makeText(context, "Erro ao cadastrar uma nova senha\nTente Novamente.", Toast.LENGTH_LONG).show() }
                        )
                    }
                },
                enabled = password.isNotBlank() && passwordNickName.isNotBlank() && category.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (password.isNotBlank() &&
                        passwordNickName.isNotBlank() &&
                        category.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(300.dp)
                    .height(80.dp)
                    .padding(bottom = 20.dp)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Salvar nova senha",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp
                        ),
                        color = if (
                            password.isNotBlank() &&
                            passwordNickName.isNotBlank() &&
                            category.isNotBlank()
                        ) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
