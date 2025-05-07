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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



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

    Scaffold { paddingValues ->
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
    var darkMode by remember { mutableStateOf(true) }
    val backgroundColor = if (darkMode) AppColors.gunmetal else Color.White
    val buttonColor = if (darkMode) AppColors.satinSheenGold else AppColors.platinum

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { DrawerContent(darkMode = darkMode) }
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
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange
                )
            },
            containerColor = backgroundColor,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = buttonColor,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, "Floating action button.")
                }
            }
        ) { paddingValues ->
            ScreenContent(
                paddingValues = paddingValues,
                darkMode = darkMode,
                searchQuery = searchQuery
            )
        }
    }
}

@Composable
fun DrawerContent(darkMode: Boolean) {
    val backgroundColor = if (darkMode) AppColors.jet else AppColors.platinum

    Column(
        modifier = Modifier
            .width(280.dp) // o quanto o Drawer abre
            .fillMaxHeight()
            .background(backgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_superid_black),
                tint = if (darkMode) AppColors.platinum else AppColors.jet,
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )
            Text(
                text = "SuperID",
                color = if(darkMode) AppColors.platinum else AppColors.jet,
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
            onClick = { TODO() } ,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = backgroundColor,
                unselectedContainerColor = backgroundColor,
                unselectedIconColor = if (darkMode) AppColors.platinum else AppColors.jet,
                unselectedTextColor = if (darkMode) AppColors.platinum else AppColors.jet,
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
            onClick = { TODO() },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = backgroundColor,
                unselectedContainerColor = backgroundColor,
                unselectedIconColor = if (darkMode) AppColors.platinum else AppColors.jet,
                unselectedTextColor = if (darkMode) AppColors.platinum else AppColors.jet,
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
            onClick = { TODO() },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = backgroundColor,
                unselectedContainerColor = backgroundColor,
                unselectedIconColor = if (darkMode) AppColors.platinum else AppColors.jet,
                unselectedTextColor = if (darkMode) AppColors.platinum else AppColors.jet,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ScreenContent(paddingValues: PaddingValues, darkMode: Boolean, searchQuery: String) {
    val senhas = remember { mutableStateListOf<SenhaData>() }
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
            RowFilter()
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
                darkMode = darkMode,
                onDelete = { deletePassword(item.id) }
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun CardItem(apelido: String, login: String, senha: String, descricao: String,
             categoria: String, idSenha: String, darkMode: Boolean,onDelete: () -> Unit) {
    var showDropdown by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(AppColors.platinum) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (darkMode) AppColors.jet else AppColors.satinSheenGold)
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
                    color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color = selectedColor)
                            .clickable { showDropdown = true }
                    )
                    ColorPickerDropdown(
                        darkMode = darkMode,
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        onColorSelected = { selectedColor = it },
                    )
                }

                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal
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
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = senha,
                style = MaterialTheme.typography.titleMedium,
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = categoria,
                style = MaterialTheme.typography.bodyMedium,
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawrer: () -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        TopAppBar(
            modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(100.dp)),
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (darkMode) AppColors.jet else AppColors.platinum
            ),
            windowInsets = WindowInsets(0.dp),
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Procure por sua Senha", fontSize = 12.sp, color = if (darkMode) AppColors.platinum else AppColors.jet)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = if (darkMode) AppColors.platinum else AppColors.jet,
                        unfocusedTextColor = if (darkMode) AppColors.platinum else AppColors.jet,
                        cursorColor = if (darkMode) AppColors.satinSheenGold else AppColors.jet,
                        focusedContainerColor = if (darkMode) AppColors.jet else AppColors.platinum,
                        unfocusedContainerColor = if (darkMode) AppColors.jet else AppColors.platinum,
                        focusedIndicatorColor = if (darkMode) AppColors.satinSheenGold else AppColors.jet,
                        unfocusedIndicatorColor = if (darkMode) AppColors.satinSheenGold else AppColors.jet,
                    )
                )
            },
            navigationIcon = {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = if (darkMode) AppColors.platinum else AppColors.jet,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp).size(27.dp).clickable { onOpenDrawrer() }
                )
            },
            actions = {
                Switch(darkMode = darkMode, onCheckedChange = onDarkModeChange)
                var expanded by remember { mutableStateOf(false) }
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = if (darkMode) AppColors.platinum else AppColors.jet,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp).size(30.dp).clickable { expanded = true }
                )
                DropdownMenuWithDetails(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                )
            }
        )
    }
}



@Composable
fun Switch(
    darkMode: Boolean,
    onCheckedChange: (Boolean) -> Unit // Função para alterar o estado do darkMode
) {
    Switch(
        checked = darkMode,
        onCheckedChange = onCheckedChange, // Alterando o estado com o callback
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF000000),
            checkedTrackColor = AppColors.gunmetal,
            uncheckedThumbColor = Color(0xFFFFFFFF),
            uncheckedTrackColor = AppColors.satinSheenGold
        )
    )
}

@Composable
fun DropdownMenuWithDetails(
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Profile") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            onClick = { /* Do something... */ }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Settings") },
            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            onClick = { /* Do something... */ }
        )

        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("About") },
            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            onClick = { /* Do something... */ }
        )
        DropdownMenuItem(
            text = { Text("Help") },
            onClick = { /* Do something... */ }
        )
    }
}

@Composable
fun RowFilter() {
    val filtros = remember {
        mutableStateListOf(
            "Entretenimento" to Color(0xFFD1A740),
            "Faculdade" to Color(0xFF38C5D9),
            "Lazer" to Color(0xFF9C27B0),
            "Saúde" to Color(0xFF4CAF50),
            "Trabalho" to Color(0xFFFF9800),
            "Sem Filtro" to AppColors.platinum
        )
    }

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
            items(filtros, key = { it.first }) { filtro ->
                Filter(
                    text = filtro.first,
                    backgroundColor = filtro.second,
                    onRemove = {
                        if (filtro.first != "Sem Filtro") {
                            filtros.remove(filtro)
                        }
                    },
                    canBeRemoved = filtro.first != "Sem Filtro"
                )
            }
        }

        IconButton(
            onClick = { /* ação do botão + */ },
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFE0E0E0), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar filtro",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun Filter(
    text: String,
    backgroundColor: Color,
    onRemove: () -> Unit,
    canBeRemoved: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, color = Color.Black)
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
fun ColorPickerDropdown(
    darkMode: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val filtros = listOf(
        "Entretenimento" to Color(0xFFD1A740),
        "Faculdade" to Color(0xFF38C5D9),
        "Lazer" to Color(0xFF9C27B0),
        "Saúde" to Color(0xFF4CAF50),
        "Trabalho" to Color(0xFFFF9800),
        "Sem Filtro" to AppColors.platinum
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.background(if (darkMode) AppColors.black else AppColors.white) // Cor de fundo do menu
    ) {
        filtros.forEach { filtro ->
            DropdownMenuItem(
                onClick = {
                    onColorSelected(filtro.second)
                    onDismissRequest()
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(filtro.second, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = filtro.first, color = if (darkMode) AppColors.white else AppColors.black ) // cor do texto do menu
                    }
                }
            )
            HorizontalDivider()
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