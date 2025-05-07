package br.com.superid

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


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

//função que salva a senha em uma subcoleçao do usuario
fun savePasswordToDb(
    user: FirebaseUser,
    login: String,
    password: String,
    description: String?,
    category: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
){

    val db = Firebase.firestore

    val UID = user.uid

    val passwordData = hashMapOf(
        "login" to login,
        "senha" to password,
        "descrição" to description,
        "categoria" to category
        // "accessToken" to alguma variavel precisa implementar o gerador do accessToken
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
fun DropDown(selectedText: String, onCategorySelected: (String) -> Unit){

    val list = listOf("Sites Web","Aplicativos","Teclados de Acesso Físico")

    var isExpanded by remember { mutableStateOf(false) }

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
                        fontFamily = PoppinsFonts.regular
                    )
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(10.dp)
                    .menuAnchor(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = AppColors.gunmetal,
                    unfocusedIndicatorColor = AppColors.platinum,
                    containerColor = Color.Transparent,
                    focusedLabelColor = AppColors.gunmetal,
                    cursorColor = AppColors.gunmetal
                )
            )

            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false } ) {
                list.forEach{ item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                item,
                                fontFamily = PoppinsFonts.medium,
                                fontSize = 12.sp,
                                color = AppColors.gunmetal
                            )
                        },
                        onClick = {

                            onCategorySelected(item)
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

    }

}

@Composable
fun CadastroSenhaScreen(){

    val auth = Firebase.auth
    val user = auth.currentUser

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    var shouldNavigate by remember { mutableStateOf(false) }

    LaunchedEffect(shouldNavigate) {
        if(shouldNavigate){
            kotlinx.coroutines.delay(1500)
            //adicionar rota futura
        }
    }

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            // preciso da ajuda para implementra o back btn ScreenBackButton(navController, context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppColors.white)
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
                text = "Cadastre uma nova senha:",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(login,{ newlogin -> login = newlogin },"Digite seu login")

            Spacer(modifier = Modifier.size(16.dp))

            passwordInputBox(password, { newPassword -> password = newPassword }, "Digite sua senha")

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(description,{ newDescription -> description = newDescription},"Digite sua descrição (opcional)")

            Spacer(modifier = Modifier.size(16.dp))

            DropDown(category, onCategorySelected = { newCategory -> category = newCategory})

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isLoading = true
                    if(user != null){
                        savePasswordToDb(
                            user,
                            login,
                            password,
                            description,
                            category,
                            onSuccess = { shouldNavigate = true },
                            onFailure = { /* Tratar erro */ }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (password.isNotBlank()) AppColors.gunmetal else AppColors.jet,
                    contentColor = AppColors.white
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(300.dp)
                    .height(80.dp)
                    .padding(bottom = 20.dp)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.white,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Salvar novo login",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 20.sp,
                        color = if (password.isNotBlank() &&
                            !shouldNavigate) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }
        }
    }
}

