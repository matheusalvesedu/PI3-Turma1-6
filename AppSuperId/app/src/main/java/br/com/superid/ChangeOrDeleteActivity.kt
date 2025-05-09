package br.com.superid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import br.com.superid.ui.theme.AppColors
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
                ChangePasswordPreview(senhaId ?: "")
            }
        }
    }
}

@Preview
@Composable
fun ChangePasswordPreview(senhaId: String = ""){
    ChangePassword(senhaId = senhaId,modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
}

@Composable
fun ChangePassword(senhaId: String, modifier: Modifier){

    var nPassword by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }
    var activity = LocalActivity.current
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (userId != null) {
            val docRef = db.collection("accounts")
                .document(userId)
                .collection("Senhas")
                .document(senhaId)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    login = document.getString("login") ?: ""
                    senha = document.getString("senha") ?: ""
                    descricao = document.getString("descrição") ?: ""
                    categoria = document.getString("categoria") ?: ""
                }
            }
        }
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
                text = "Alterar Senha",
                fontFamily = PoppinsFonts.medium,
                fontSize = 24.sp,
                color = AppColors.gunmetal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))


            Spacer(modifier = Modifier.size(16.dp))

            passwordInputBox(
                nPassword,
                { newPassword -> nPassword = newPassword },
                "Digite sua nova senha"
            )

            Spacer(modifier = Modifier.size(16.dp))

            inputBox(
                description,
                { newDescription -> description = newDescription },
                "Digite sua descrição (opcional)"
            )

            Spacer(modifier = Modifier.size(16.dp))

            DropDown(category, onCategorySelected = { newCategory -> category = newCategory })

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (userId != null) {
                        senha = nPassword
                        db.collection("accounts")
                            .document(userId)
                            .collection("Senhas")
                            .document(senhaId)
                            .update(
                                mapOf(
                                    "login" to login,
                                    "senha" to senha,
                                    "descrição" to description,
                                    "categoria" to category
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(context, "Senha atualizada!", Toast.LENGTH_SHORT)
                                    .show()
                                mudarTela(context, PrincipalScreenActivity::class.java)
                            }
                    }

                }

                ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (nPassword.isNotBlank()) AppColors.gunmetal else AppColors.jet,
                    contentColor = AppColors.white
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(300.dp)
                    .height(80.dp)
                    .padding(bottom = 20.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.white,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Salvar",
                        fontFamily = PoppinsFonts.medium,
                        fontSize = 20.sp,
                        color = if (nPassword.isNotBlank() && !shouldNavigate) AppColors.platinum else AppColors.gunmetal
                    )
                }
            }

        }
    }
}

