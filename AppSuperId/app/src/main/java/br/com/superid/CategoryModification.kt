package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoryModification : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                PreviewCategoryMod()
            }
        }
    }
}

@Composable
fun CaegoryModFlow(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "name") {

        composable("name") { NameScreen(navController) }

        composable("email/{name}") { entry ->
            entry.arguments?.getString("name")?.let { name ->
                EmailScreen(navController,name)
            }
        }

        composable("password/{name}/{email}") { entry->
            entry.arguments?.getString("name")?.let { name ->
                entry.arguments?.getString("email")?.let{ email ->
                    PasswordScreen(navController,name,email)
                }
            }

        }

        composable("verification/{name}/{email}"){ entry ->
            entry.arguments?.getString("name")?.let { name->
                entry.arguments?.getString("email")?.let{ email ->
                    VerificationScreen(navController,name,email)
                }
            }
        }

        composable("home"){HomeScreen(navController)}
    }
}

fun getCategorias(userId: String, context: Context, onResult: (List<String>) -> Unit) {
    val db = Firebase.firestore

    db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .get()
        .addOnSuccessListener { result ->
            val nomes = result.documents
                .mapNotNull { it.getString("nome") }
                .filter { it != "Sites Web" }
            onResult(nomes)
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao buscar categorias no Firestore.", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
}

@Composable
fun PreviewCategoryMod() {
    CategoryMod()
}

@SuppressLint("ContextCastToActivity")
@Preview(showBackground = true)
@Composable
fun CategoryMod() {
    val auth = Firebase.auth
    val user = auth.currentUser
    val uid = user?.uid

    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    var categorias by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(uid) {
        uid?.let{
            getCategorias(it, context) { resultado ->
                categorias = resultado
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top
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

            Text(text = "Escolha uma categoria para alterar:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Suas senhas:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp)
            )

            TextButton(
                onClick = { Toast.makeText(context, "Categoria não alterável." , Toast.LENGTH_LONG).show()},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sites Web",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            categorias.forEach{ categoria ->
                TextButton(
                    onClick = {

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }

        }
    }
}