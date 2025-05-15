package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
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

        composable("editCategory/{idDaCategoria}") { entry ->
            val idDaCategoria = entry.arguments?.getString("idDaCategoria")
            idDaCategoria?.let {
                EditCategoryScreen(navController, idDaCategoria = it)
            }
        }

        composable("newCategory") {
            NewCategoryScreen(navController)
        }

        composable("editColor") {
            EditCategoryColorScreen(navController)
        }
    }
}

fun adicionarCategoria(
    userId: String,
    nome: String,
    cor: String,
    context: Context,
    navController: NavController,
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
){
    val db = Firebase.firestore

    var novaCor = cor

    if(novaCor == ""){
        novaCor = "0xFFFFFFFF"
    }

    val novaCategoria = hashMapOf(
        "Nome" to nome,
        "Cor" to novaCor
    )

    db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .add(novaCategoria)
        .addOnSuccessListener {
            Toast.makeText(context, "Categoria adicionada com sucesso.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Falha ao adicionar categoria ao banco.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
}

fun alterarCategoria(userId: String,
                     idCategoria: String,
                     novoNome: String,
                     novaCor: String,
                     context: Context,
                     navController: NavController,
                     onSuccess: () -> Unit = {},
                     onFailure: () -> Unit = {}
){
    val db = Firebase.firestore

    val atualizacoes = mutableMapOf<String, Any>()

    if (novoNome.isNotBlank()) atualizacoes["Nome"] = novoNome
    if (novaCor.isNotBlank()) atualizacoes["Cor"] = novaCor

    db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .document(idCategoria)
        .update(atualizacoes)
        .addOnSuccessListener {
            Toast.makeText(context, "Categoria atualizada.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao atualizar a categoria.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
}

fun excluirCategoria(userId: String,
                     idCategoria: String,
                     context: Context,
                     navController: NavController,
                     onSuccess: () -> Unit = {},
                     onFailure: () -> Unit = {}
){
    val db = Firebase.firestore
    val document = db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .document(idCategoria)

    document.delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Categoria excluída.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            navController.navigate("categoryList")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao excluir categoria.", Toast.LENGTH_SHORT).show()
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun CategoriesListScreen(navController: NavController) {
    val auth = Firebase.auth
    val user = auth.currentUser
    val uid = user?.uid ?: return

    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    var categoriaParaExcluir by remember { mutableStateOf<Categoria?>(null) }

    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    LaunchedEffect(uid) {
        uid?.let{
            getCategorias(it, context) { resultado ->
                categorias = resultado
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { activityBackButton(activity) }
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

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suas categorias",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(0.9f)
                )

                IconButton(
                    onClick = {navController.navigate("newCategory")},
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar categoria"
                    )
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Sites Web",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                )

                IconButton(
                    onClick = { Toast.makeText(context, "Categoria não alterável." , Toast.LENGTH_LONG).show() },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar categoria"
                    )
                }

                IconButton(
                    onClick = { Toast.makeText(context, "Categoria não excluível.", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir categoria"
                    )
                }
            }

            categorias.forEach{ categoria ->
                if (categoria.nome != "Sites Web"){
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = categoria.nome,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.8f)
                        )

                        IconButton(
                            onClick = { navController.navigate("editCategory/${categoria.id}") },
                            modifier = Modifier.weight(0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar categoria"
                            )
                        }

                        IconButton(
                            onClick = { categoriaParaExcluir = categoria },
                            modifier = Modifier.weight(0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Excluir categoria"
                            )
                        }
                    }
                }

                if (categoriaParaExcluir != null) {
                    AlertDialog(
                        onDismissRequest = { categoriaParaExcluir = null },
                        title = { Text("Confirmar exclusão") },
                        text = { Text("Tem certeza de que deseja excluir esta categoria?") },
                        confirmButton = {
                            TextButton(onClick = {
                                excluirCategoria(uid, categoriaParaExcluir!!.id, context, navController)
                                categoriaParaExcluir = null
                            }) {
                                Text("Sim")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { categoriaParaExcluir = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(navController: NavController, idDaCategoria: String){
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val uid = user?.uid ?: return

    var nomeAtual by remember { mutableStateOf("") }
    var novoNome by rememberSaveable { mutableStateOf("") }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val novaCorState = savedStateHandle?.getStateFlow("novaCor", "")
    val novaCor by novaCorState?.collectAsState() ?: remember { mutableStateOf("") }

    val db = Firebase.firestore

    LaunchedEffect(idDaCategoria) {
        db.collection("accounts")
            .document(uid)
            .collection("Categorias")
            .document(idDaCategoria)
            .get()
            .addOnSuccessListener { document ->
                val nome = document.getString("Nome") ?: ""
                nomeAtual = nome
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar categoria.", Toast.LENGTH_SHORT).show()
            }
    }

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

            Text(text = "Alterar a categoria ${nomeAtual}",
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
                onClick = {
                    navController.navigate("editColor")
                }
            ) {
                Text(
                    text = "Alterar cor"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { alterarCategoria(uid, idDaCategoria, novoNome, novaCor, context, navController)}
            ) {
                Text(
                    text = "Salvar",
                )
            }
        }
    }
}

@Composable
fun NewCategoryScreen(navController: NavController){
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val uid = user?.uid ?: return

    var nome by rememberSaveable { mutableStateOf("") }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val novaCorState = savedStateHandle?.getStateFlow("novaCor", "")
    val novaCor by novaCorState?.collectAsState() ?: remember { mutableStateOf("") }

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

            Text(
                text = "Adicione uma nova categoria",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            inputBox(nome, {novoNome -> nome = novoNome}, "Digite o nome")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("editColor")
                }
            ) {
                Text(
                    text = "Alterar cor"
                )
            }

            Button(
                onClick = { adicionarCategoria(uid, nome, novaCor, context, navController) }
            ) {
                Text(
                    text = "Salvar",
                )
            }

        }
    }
}

@Composable
fun EditCategoryColorScreen(navController: NavController){
    val context = LocalContext.current

    val controller = remember { ColorPickerController() }
    var selectedColor by remember { mutableStateOf(Color.White) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenBackButton(navController, context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Escolha uma nova cor",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                onColorChanged = {
                    selectedColor = it.color
                },
                controller = controller
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor
                )
            ){
                Text(
                    text = "Exemplo",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AppColors.gunmetal
                    )
                )
            }

            Button(
                onClick = {
                    val colorHex = "0x" + selectedColor.toArgb().toUInt().toString(16).uppercase()
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("novaCor", colorHex)
                    navController.popBackStack()
                }
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}