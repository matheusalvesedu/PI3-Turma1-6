package br.com.superid

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
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.draw.clip
import br.com.superid.ui.theme.AppColors
import br.com.superid.ui.theme.SuperIDTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
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
//TODO ligar a CadastroSenhaActivity no botão de adicionar senha
data class SenhaData(
    val apelido: String = "",
    val login: String = "",
    val senha: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val id: String = ""
)

data class CategoriaData(
    val nomeCategoria: String,
    val corCategoria: String // ou Color se quiser já convertido
)



@Preview
@Composable
fun TelaPrincipalPreview() {
    Box(modifier = Modifier.height(800.dp)) {
        Tela()
    }
}

@Composable
fun Tela() {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        Modifier.background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->
        Screen(
            modifier = Modifier.padding(paddingValues),
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var context = LocalContext.current
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { DrawerContent() }
        }
    ) {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    onOpenDrawrer = {
                        scope.launch {
                            drawerState.apply { if (isClosed) open() else close() }
                        }
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange
                )
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
                searchQuery = searchQuery
            )
        }
    }
}

@Composable
fun DrawerContent() {

    Column(
        modifier = Modifier
            .width(280.dp) // o quanto o Drawer abre
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_superid_black),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )
            Text(
                text = "SuperID",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        HorizontalDivider()
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Account",
                    modifier = Modifier.size(27.dp)
                )
            },
            label = {
                Text(
                    text = "Account",
                    fontSize = 17.sp,
                )
            },
            selected = false,
            onClick = { /*TODO()*/ } ,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(27.dp)
                )
            },
            label = {
                Text(
                    text = "Notifications",
                    fontSize = 17.sp,
                )
            },
            selected = false,
            onClick = { /*TODO()*/ },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = "Inbox",
                    modifier = Modifier.size(27.dp)
                )
            },
            label = {
                Text(
                    text = "Inbox",
                    fontSize = 17.sp,
                )
            },
            selected = false,
            onClick = { /*TODO()*/ },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ScreenContent(paddingValues: PaddingValues, searchQuery: String) {
    val senhas = remember { mutableStateListOf<SenhaData>() }
    val categorias = remember { mutableStateListOf<CategoriaData>() }
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    // Carregar os dados do Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("accounts")
                .document(userId)
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
                        val idSenha = document.toObject(SenhaData::class.java).copy(id = document.id)
                        senhas.add(SenhaData(apelido, login, senha, descricao, categoria, idSenha.id))
                    }
                }
        }
    }

    // Carregar os dados das categorias
    DisposableEffect(userId) { // A chave é userId: se mudar, o efeito anterior é descartado e um novo é configurado
        val registration: ListenerRegistration? = if (userId != null) {
            // Configura o listener somente se userId não for nulo
            db.collection("accounts")
                .document(userId)
                .collection("Categorias")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        categorias.clear() // Limpa a lista atual
                        for (document in snapshot.documents) {
                            val nomeCategoria = document.getString("Nome") ?: ""
                            val corCategoria = document.getString("Cor") ?: ""
                            // Adiciona na ordem correta
                            categorias.add(CategoriaData(nomeCategoria, corCategoria))
                        }
                        // A UI que observa 'categorias' será recomposta automaticamente.
                    }
                }
        } else {
            // Se userId for nulo, não configura listener
            null
        }
        // Este é o bloco onDispose nativo do DisposableEffect
        // Ele é executado quando o efeito "sai" (quando o Composable é removido ou a chave muda)
        onDispose {
            // Remove o listener se ele foi configurado
            registration?.remove()
        }
    }


    fun deletePassword(idSenha: String) {
        if (userId != null) {
            db.collection("accounts")
                .document(userId)
                .collection("Senhas")
                .document(idSenha)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    senhas.removeAll { it.id == idSenha }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao excluir senha: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val filteredSenhas = if (searchQuery.isBlank()) {
        senhas
    } else {
        senhas.filter { it.apelido.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = 16.dp
        )
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            RowFilter(
                categorias = categorias,
                onEditCategorias = { mudarTela(context, CategoryModification::class.java) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(filteredSenhas.size) { index ->
            val item = filteredSenhas[index]
            CardItem(
                apelido = item.apelido,
                login = "Login: ${item.login}",
                senha = "Password: ${item.senha}",
                descricao = "Descrição: ${item.descricao}",
                categoria = "Categoria: ${item.categoria}",
                idSenha = item.id,
                onDelete = { deletePassword(item.id) }
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun CardItem(apelido: String, login: String, senha: String, descricao: String,
             categoria: String, idSenha: String, onDelete: () -> Unit) {
    var showDropdown by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(AppColors.platinum) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(220.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color = selectedColor)
                            .clickable { showDropdown = true }
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
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
                ConfirmDeleteDialog(
                    onConfirm = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    onDismiss = {
                        showDeleteDialog = false
                    }
                )
            }


            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = login,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = senha,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawrer: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        TopAppBar(
            modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(100.dp)),
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            windowInsets = WindowInsets(0.dp),
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Procure por sua Senha", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            },
            navigationIcon = {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp).size(27.dp).clickable { onOpenDrawrer() }
                )
            },
            actions = {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_black),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )
            }
        )
    }
}

@Composable
fun RowFilter(
    categorias: List<CategoriaData>, // agora recebe uma lista de categorias
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
                val color = try {
                    val hex = categoria.corCategoria.removePrefix("0x") // Remove o prefixo "0x"
                    val argb = hex.toULong(16) // Converte para ULong

                    // Extrai os componentes ARGB usando ULong literals (0xFFuL)
                    val alpha = ((argb shr 24) and 0xFFuL).toFloat() / 255f
                    val red = ((argb shr 16) and 0xFFuL).toFloat() / 255f
                    val green = ((argb shr 8) and 0xFFuL).toFloat() / 255f
                    val blue = (argb and 0xFFuL).toFloat() / 255f

                    // Cria o objeto Color usando os componentes individuais
                    Color(red, green, blue, alpha)

                } catch (e: Exception) {
                    // Se houver qualquer erro na conversão (por exemplo, string inválida), usa uma cor padrão
                    println("Erro ao converter cor hexadecimal '${categoria.corCategoria}' usando componentes ARGB: ${e.message}")
                    AppColors.platinum // cor padrão
                }

                Filter(
                    nomeCategoria = categoria.nomeCategoria,
                    corCategoria = color, // Passa o Color object resultante
                    onRemove = { /* sua lógica de remoção, se aplicável */ },
                    canBeRemoved = false // ajuste conforme a necessidade de remover filtros
                )
            }
        }

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
    onRemove: () -> Unit,
    canBeRemoved: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(corCategoria) // cor da categoria real
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
                    tint = Color.Black,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}



@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.white)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tem certeza que deseja excluir esta senha?",
                    fontSize = 18.sp,
                    color = AppColors.gunmetal,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.satinSheenGold,
                            contentColor = AppColors.black
                        )
                    ) {
                        Text("Sim")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.gunmetal,
                            contentColor = AppColors.white
                        )
                    ) {
                        Text("Não")
                    }
                }
            }
        }
    }
}