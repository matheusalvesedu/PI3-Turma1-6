package br.com.superid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeOrDeleteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                val senhaId = intent.getStringExtra("senhaId") ?: ""
                ChangePasswordPreview(senhaId)
            }
        }
    }
}

@Preview
@Composable
fun ChangePasswordPreview(senhaId: String = "") {
    ChangePassword(
        senhaId = senhaId,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun ChangePassword(senhaId: String, modifier: Modifier) {

    var nPassword by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val activity = LocalActivity.current
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var apelidocategoria by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (userId != null) {
            val docRef = db.collection("accounts")
                .document(userId)
                .collection("Senhas")
                .document(senhaId)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    apelidocategoria = document.getString("Apelido da senha") ?: ""
                    login = document.getString("login") ?: ""
                    senha = aesDecryptWithKey(document.getString("senha") ?: "")
                    descricao = document.getString("descrição") ?: ""
                    categoria = document.getString("categoria") ?: ""

                    description = descricao
                    category = categoria
                    nPassword = senha
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            activityBackButton(activity)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Alterar Dados",
                fontSize = 24.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(
                apelidocategoria,
                { newApelido -> apelidocategoria = newApelido },
                "Digite um apelido para a senha"
            )

            inputBox(
                login,
                { newLogin -> login = newLogin},
                "Digite seu novo login"
            )

            passwordInputBox(
                nPassword,
                { newPassword -> nPassword = newPassword },
                "Digite sua nova senha"
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBoxMaxLength(description,
                { newDescription -> description = newDescription },
                "Digite uma descrição (opcional)", 150)

            Spacer(modifier = Modifier.size(16.dp))

            if (user != null) {
                DropDown(user, context, category) { newCategory ->
                    category = newCategory
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (userId != null) {
                        isLoading = true
                        senha = nPassword
                        db.collection("accounts")
                            .document(userId)
                            .collection("Senhas")
                            .document(senhaId)
                            .update(
                                mapOf(
                                    "Apelido da senha" to apelidocategoria,
                                    "login" to login,
                                    "senha" to aesEncryptWithKey(senha),
                                    "descrição" to description,
                                    "categoria" to category
                                )
                            )
                            .addOnSuccessListener {
                                Toast
                                    .makeText(context, "Senha atualizada!", Toast.LENGTH_SHORT)
                                    .show()
                                 activity?.finish()
                            }
                    }
                },
                enabled = nPassword.isNotBlank() && apelidocategoria.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (nPassword.isNotBlank() && apelidocategoria.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(300.dp)
                    .height(80.dp)
                    .padding(bottom = 20.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Salvar",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (
                            nPassword.isNotBlank() && apelidocategoria.isNotBlank()
                        ) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
