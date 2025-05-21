package br.com.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import br.com.superid.ui.theme.SuperIDTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.media3.common.util.UnstableApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PrincipalScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme(dynamicColor = false) {
                TelaPrincipalPreview()
            }
        }
    }
}

@Preview
@Composable
fun TelaPrincipalPreview() {
    var searchQuery by remember { mutableStateOf("") }
    var shouldReload by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        TelaPrincipal(modifier = Modifier,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            shouldReload = shouldReload,
            onReloadChange = { shouldReload = it }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    shouldReload: Boolean,
    onReloadChange: (Boolean) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    var context = LocalContext.current
    var expandedLogout by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Column {
                Spacer(modifier = Modifier.fillMaxHeight(0.04f))

                TopAppBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            placeholder = {
                                Text("Procure por sua Senha", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpar busca"
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar"
                                    )
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        Box {
                            IconButton(
                                onClick = {expandedLogout = true}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Conta",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = expandedLogout,
                                onDismissRequest = { expandedLogout = false }
                            ) {

                                DropdownMenuItem(
                                    text = { Text("Verifique sua conta") },
                                    onClick = {
                                        expandedLogout = false

                                        currentUser?.sendEmailVerification()
                                        Toast.makeText(context, "E-mail de verificação reenviado!", Toast.LENGTH_LONG).show()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Sair") },
                                    onClick = {
                                        expandedLogout = false
                                        FirebaseAuth.getInstance().signOut()
                                        Toast.makeText(context, "Sessão encerrada.", Toast.LENGTH_SHORT).show()
                                        val userSharedPreferences = context.getSharedPreferences("user_prefs",Context.MODE_PRIVATE)
                                        userSharedPreferences.edit { putBoolean("is_logged", false) }
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                currentUser?.reload()
                                if(currentUser?.isEmailVerified == true){
                                    mudarTela(context, QRCodeActivity::class.java)
                                }else{
                                    Toast.makeText(context, "É necessário verificar o e-mail para utilizar esta funcionalidade.", Toast.LENGTH_SHORT).show()
                                }

                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear QR Code",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    mudarTela(context, CadastroSenhaActivity::class.java)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Filled.Add,
                    "Floating action button.",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        ScreenContent(
            paddingValues = paddingValues,
            searchQuery = searchQuery,
            shouldReload = shouldReload,
            onReloadChange = onReloadChange
        )
    }
}

@Composable
fun ScreenContent(
    paddingValues: PaddingValues,
    searchQuery: String,
    shouldReload: Boolean,
    onReloadChange: (Boolean) -> Unit
) {
    val senhas = remember { mutableStateListOf<SenhaData>() }
    val categorias = remember { mutableStateListOf<CategoriaData>() }
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showVerificationBanner by remember { mutableStateOf(true) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var dataLoadedSuccessfully by remember { mutableStateOf(false) }

    val filteredSenhas = remember(senhas, searchQuery, selectedCategory) {
        senhas.filter { senha ->
            val matchesSearch = searchQuery.isBlank() || senha.apelido.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || senha.categoria == selectedCategory
            matchesSearch && matchesCategory
        }.toMutableStateList()
    }

    LaunchedEffect(currentUser, shouldReload) {
        if (currentUser != null) {
            isLoading = true
            db.collection("accounts")
                .document(currentUser.uid)
                .collection("Senhas")
                .get()
                .addOnSuccessListener { result ->
                    senhas.clear()
                    for (document in result) {
                        val apelido = document.getString("Apelido da senha") ?: ""
                        val login = document.getString("login") ?: ""
                        val senha = document.getString("senha") ?: ""
                        val descricao = document.getString("descrição") ?: ""
                        val categoria = document.getString("categoria") ?: ""
                        val idSenha = document.id
                        senhas.add(SenhaData(apelido, login, senha, descricao, categoria, idSenha))
                    }
                    filteredSenhas.clear()
                    filteredSenhas.addAll(senhas.filter { senha ->
                        val matchesSearch = searchQuery.isBlank() || senha.apelido.contains(searchQuery, ignoreCase = true)
                        val matchesCategory = selectedCategory == null || senha.categoria == selectedCategory
                        matchesSearch && matchesCategory
                    })
                    isLoading = false
                    dataLoadedSuccessfully = true
                    onReloadChange(false)
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    dataLoadedSuccessfully = false
                    Toast.makeText(context, "Erro ao carregar senhas: ${e.message}", Toast.LENGTH_SHORT).show()
                    onReloadChange(false)
                }
        } else {
            isLoading = false
            dataLoadedSuccessfully = false
            onReloadChange(false)
        }
    }

    DisposableEffect(currentUser) {
        val registration: ListenerRegistration? = if (currentUser != null) {
            db.collection("accounts")
                .document(currentUser.uid)
                .collection("Categorias")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        categorias.clear()
                        for (document in snapshot.documents) {
                            val nomeCategoria = document.getString("Nome") ?: ""
                            val corCategoria = document.getString("Cor") ?: ""
                            categorias.add(CategoriaData(nomeCategoria, corCategoria))
                        }
                    }
                }
        } else {
            null
        }
        onDispose {
            registration?.remove()
        }
    }

    fun deletePassword(idSenha: String) {
        if (currentUser != null) {
            db.collection("accounts")
                .document(currentUser.uid)
                .collection("Senhas")
                .document(idSenha)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    senhas.removeAll { it.id == idSenha }
                    onReloadChange(true)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao excluir senha: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (dataLoadedSuccessfully || senhas.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        bottom = 16.dp
                    )
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        RowFilter(
                            categorias = categorias,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { category -> selectedCategory = category },
                            onEditCategorias = { mudarTela(context, CategoryModification::class.java) }
                        )
                        
                        // Modified verification reminder
                        if (currentUser?.isEmailVerified == false && showVerificationBanner) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showVerificationDialog = true }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Verifique seu e-mail para ter acesso a todas as funcionalidades e não perder sua conta.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { showVerificationBanner = false }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fechar",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(filteredSenhas.size) { index ->
                        val item = filteredSenhas[index]
                        CardItem(
                            apelido = item.apelido,
                            login = "Login: ${item.login}",
                            senha = aesDecryptWithKey(item.senha),
                            descricao = "Descrição: ${item.descricao}",
                            categoria = "Categoria: ${item.categoria}",
                            idSenha = item.id,
                            categorias = categorias,
                            onDelete = { deletePassword(item.id) }
                        )
                    }
                }
            } else {
                Text("Não foi possível carregar as senhas ou não há senhas cadastradas.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun CardItem(apelido: String,
             login: String,
             senha: String,
             descricao: String,
             categoria: String,
             idSenha: String,
             categorias: List<CategoriaData>,
             onDelete: () -> Unit) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val nomeCategoriaLimpo = categoria.removePrefix("Categoria: ").trim()
    val categoriaCerta = categorias.find {
        it.nomeCategoria.trim().equals(nomeCategoriaLimpo, ignoreCase = true)
    }

    var senhaVisivel by remember { mutableStateOf(false) }

    val corFilro = hexToColor(categoriaCerta?.corCategoria)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = apelido,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color = corFilro)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                    )

                }

                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Alterar senha") },
                            onClick = {
                                showOptionsMenu = false
                                val intent = Intent(context, ChangeOrDeleteActivity::class.java)
                                intent.putExtra("senhaId", idSenha)
                                context.startActivity(intent)

                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir") },
                            onClick = {
                                showOptionsMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirmar exclusão") },
                    text = { Text("Tem certeza de que deseja excluir este item?") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDelete()
                            showDeleteDialog = false
                        }) {
                            Text("Sim")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = login,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Password: " + if (senhaVisivel) senha else "•".repeat(senha.length),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { senhaVisivel = !senhaVisivel }
                ) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        modifier = Modifier.size(18.dp),
                        contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                    )
                }
            }

            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = categoria,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun RowFilter(
    categorias: List<CategoriaData>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onEditCategorias: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(categorias) { categoria ->
                if(categoria.nomeCategoria != "Sem Categoria"){
                    val color = hexToColor(categoria.corCategoria)

                    Filter(
                        nomeCategoria = categoria.nomeCategoria,
                        corCategoria = color,
                        isSelected = categoria.nomeCategoria == selectedCategory,
                        onClick = {
                            onCategorySelected(if (selectedCategory == categoria.nomeCategoria) null else categoria.nomeCategoria)
                        },
                        onRemove = {},
                        canBeRemoved = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(5.dp))

        IconButton(
            onClick = onEditCategorias,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar filtros",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun Filter(
    nomeCategoria: String,
    corCategoria: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    canBeRemoved: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(corCategoria)
            .then(
                if (isSelected) Modifier.border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(50)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = nomeCategoria, color = Color.Black)
            if (canBeRemoved) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover filtro",
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Black,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}
