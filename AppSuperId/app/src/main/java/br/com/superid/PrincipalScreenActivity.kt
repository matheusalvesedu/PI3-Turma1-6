package br.com.superid

import android.os.Bundle
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
    Box(modifier = Modifier.height(800.dp)) {
        Tela()
    }
}

@Composable
fun Tela() {
    Scaffold { paddingValues ->
        Screen(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    // Variável para DarkMode
    var darkMode by remember { mutableStateOf(true) } // true para escuro, false para claro

    // Cor do fundo do App
    val backgroundColor = if (darkMode) AppColors.gunmetal else Color.White // cor escura ou clara

    // Definir a cor do botão + com base no estado do darkMode
    val buttonColor = if (darkMode) AppColors.satinSheenGold else AppColors.platinum
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent()
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    onOpenDrawrer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    darkMode = darkMode, // Passa o estado do darkMode para TopBar
                    onDarkModeChange = { darkMode = it } // Função para alterar o darkMode
                )
            },
            containerColor = backgroundColor, // Define o fundo aqui
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
                darkMode = darkMode // Passa o estado do darkMode para ScreenContent
            )
        }
    }
}


@Composable
fun DrawerContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.logo_superid_black),
            contentDescription = null,
            modifier = Modifier.size(70.dp)
        )
        Text(
            text = "SuperID",
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
        onClick = { TODO() }
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
        onClick = { TODO() }
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
        onClick = { TODO() }
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun ScreenContent(paddingValues: PaddingValues, darkMode: Boolean) {
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
            RowFilter() // Chama a os Filtros
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(10) { index ->
            CardItem(
                title = "Título do Card ${index + 1}",
                user = "User : email${index + 1}@gmail.com",
                password = "Password : ******* do Card ${index + 1}",
                darkMode = darkMode
            )
        }
    }
}

@Composable
fun CardItem(title: String, user: String, password: String, darkMode: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if(darkMode) AppColors.jet else AppColors.satinSheenGold)
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal
                )

                Spacer(modifier = Modifier.width(90.dp)) // Ajustado o espaço aqui

                Box(
                    modifier = Modifier
                        .size(24.dp) // Tamanho do círculo
                        .background(
                            color = AppColors.platinum, // Colocar cor do Filtro
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user,
                style = MaterialTheme.typography.titleMedium,
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = password,
                style = MaterialTheme.typography.titleMedium,
                color = if (darkMode) AppColors.satinSheenGold else AppColors.gunmetal,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawrer: () -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        // Spacer para separar do topo da tela
        Spacer(modifier = Modifier.height(16.dp))

        TopAppBar(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(100.dp)),
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if(darkMode) AppColors.jet else AppColors.platinum
            ),
            windowInsets = WindowInsets(0.dp), // Remove o padding padrão da status bar

            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Procure por sua Senha", fontSize = 12.sp,color = if(darkMode) AppColors.platinum else AppColors.jet)
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
                    ),
                )
            },

            navigationIcon = {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = if (darkMode) AppColors.platinum else AppColors.jet,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(27.dp)
                        .clickable {
                            onOpenDrawrer()
                        }
                )
            },

            actions = {
                Switch(
                    darkMode = darkMode,
                    onCheckedChange = onDarkModeChange
                )
                var expanded by remember { mutableStateOf(false) }

                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = if (darkMode) AppColors.platinum else AppColors.jet,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 8.dp)
                        .size(30.dp)
                        .clickable {
                            expanded = true
                        }
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
            "Sem Filtro" to Color(0xFFFFFFFF)
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


