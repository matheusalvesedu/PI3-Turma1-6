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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoryModification : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                CategoryModFlow()
            }
        }
    }
}

@Composable
fun CategoryModFlow(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "categoryList") {

        composable("categoryList") {
            CategoriesListScreen(navController)
        }

        composable("editCategory/{nomeDaCategoria}") { entry ->
            val nomeDaCategoria = entry.arguments?.getString("nomeDaCategoria")
            nomeDaCategoria?.let {
                EditCategoryScreen(navController, categoria = it)
            }
        }
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
                .mapNotNull { it.getString("Nome") }
                .filter { it != "Sites Web" }
            onResult(nomes)
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao buscar categorias no Firestore.", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
}

fun alterarCategoria(userId: String,
                      categoriaAtual: String,
                      novoNome: String,
                      context: Context,
                      onSuccess: () -> Unit = {},
                      onFailure: () -> Unit = {}){
    if (novoNome.isBlank()){
        Toast.makeText(context, "Digite um novo nome.", Toast.LENGTH_SHORT).show()
        return
    }

    val db = Firebase.firestore
    val categorias = db.collection("accounts")
        .document(userId)
        .collection("Categorias")

    val documentoAtual = categorias.document(categoriaAtual)
    val documentoNovo = categorias.document(novoNome)

    documentoAtual.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val dados = snapshot.data ?: emptyMap<String, Any>()

            val novosDados = dados.toMutableMap()
            novosDados["Nome"] = novoNome

            documentoNovo.set(novosDados).addOnSuccessListener {
                documentoAtual.delete().addOnSuccessListener {
                    Toast.makeText(context, "Categoria renomeada com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    onSuccess()
                }.addOnFailureListener {
                    Toast.makeText(context, "Erro ao excluir categoria antiga.", Toast.LENGTH_SHORT)
                        .show()
                    onFailure()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Erro ao criar nova categoria.", Toast.LENGTH_SHORT).show()
                onFailure()

            }
        } else {
            Toast.makeText(context, "Categoria original não encontrada.", Toast.LENGTH_SHORT).show()
            onFailure()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Erro ao acessar categoria atual.", Toast.LENGTH_SHORT).show()
        onFailure()
    }
}

fun excluirCategoria(userId: String,
                     categoria: String,
                     context: Context,
                     onSuccess: () -> Unit = {},
                     onFailure: () -> Unit = {}){
    val db = Firebase.firestore
    val document = db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .document(categoria)

    document.delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Categoria excluída.", Toast.LENGTH_SHORT).show()
            onSuccess
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao excluir categoria.", Toast.LENGTH_SHORT).show()
            onFailure
        }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun CategoriesListScreen(navController: NavController) {
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
                        navController.navigate("editCategory/${categoria}")
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

@Composable
fun EditCategoryScreen(navController: NavController, categoria: String){
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val uid = user?.uid ?: return

    var novoNome by remember { mutableStateOf("") }
    var showPopUp by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenBackButton(navController, context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
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

            Text(text = "Alterar a categoria ${categoria}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            inputBox(novoNome, { novoNomeSup -> novoNome = novoNomeSup }, "Digite o novo nome")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {}
            ) {
                Text(
                    text = "Alterar cor"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { alterarCategoria(uid, categoria, novoNome, context)},
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .height(50.dp)
                    .padding(10.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Salvar",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showPopUp = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.error)
                    .height(50.dp)
                    .padding(10.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Excluir",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onError
                    )
                )
            }
        }

        if (showPopUp) {
            AlertDialog(
                onDismissRequest = { showPopUp = false },
                title = { Text("Confirmar exclusão") },
                text = { Text("Tem certeza de que deseja excluir esta categoria?") },
                confirmButton = {
                    TextButton(onClick = {
                        showPopUp = false
                        excluirCategoria(uid, categoria, context)
                    }) {
                        Text("Sim")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPopUp = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}