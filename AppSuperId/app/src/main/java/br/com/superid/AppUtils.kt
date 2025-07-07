package br.com.superid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.superid.ui.theme.AppColors
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import br.com.superid.R
import android.annotation.SuppressLint
import android.util.Log
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec




// Função para transicionar entre as telas
fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}

fun mudarTelaFinish(context: Context, destination: Class<*>){
    val intent = Intent(context, destination)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}

// Função para criar caixas com input
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun inputBox(variavel: String, onValueChange: (String) -> Unit, texto: String){
    TextField(
        value = variavel,
        onValueChange = onValueChange,
        label = {
            Text(
                text = texto,
                fontSize = 12.sp,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .width(300.dp)
            .padding(10.dp),
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            containerColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun inputBoxMaxLength(variavel: String, onValueChange: (String) -> Unit, texto: String, maxLength: Int = Int.MAX_VALUE) {
    Column(modifier = Modifier.width(300.dp)) {
        TextField(
            value = variavel,
            onValueChange = {
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            label = {
                Text(
                    text = texto,
                    fontSize = 12.sp,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                containerColor = Color.Transparent,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Mostra o aviso caso tenha atingido o limite
        if (variavel.length >= maxLength) {
            Text(
                text = "Limite de $maxLength caracteres atingido",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}


// Função para criar caixas com input de senha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun passwordInputBox(variavel: String, onValueChange: (String) -> Unit, texto: String){
    var isPasswordVisible by remember { mutableStateOf(false) }

    TextField(
        value = variavel,
        onValueChange = onValueChange,
        label = {
            Text(
                text = texto,
                fontSize = 12.sp,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        modifier = Modifier
            .width(300.dp)
            .padding(10.dp),
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (isPasswordVisible)
                Icons.Filled.Visibility
            else
                Icons.Filled.VisibilityOff
            val description = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha"

            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            containerColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// Botão para voltar activities
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun activityBackButton(activity: Activity?){
    TopAppBar(
        modifier = Modifier.height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {},
        navigationIcon = {
            IconButton(onClick = { activity?.finish() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
    )
}

// Botão para voltar telas dentro de uma única activity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenBackButton(navController: NavController,context: Context){
    TopAppBar(
        modifier = Modifier.height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {},
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBackButton(navController: NavController, context: Context) {
    val activity = context as? Activity
    val canNavigateBack = navController.previousBackStackEntry != null

    TopAppBar(
        modifier = Modifier.height(80.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {},
        navigationIcon = {
            IconButton(onClick = {
                if (canNavigateBack) {
                    navController.popBackStack()
                } else {
                    activity?.finish()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
    )
}

// Alias da chave, deve ser o mesmo usado na geração
private const val KEY_ALIAS = "br.com.superid.chave-mestra"
// Provedor do KeyStore
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
// Algoritmo de transformação. AES/GCM é o padrão moderno e seguro.
private const val TRANSFORMATION = "AES/GCM/NoPadding"
// Tamanho do Vetor de Inicialização (IV) em bytes. 12 bytes é o padrão para GCM.
private const val IV_SIZE_BYTES = 12

private fun getSecretKey(): SecretKey? {
    return try {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    } catch (e: Exception) {
        Log.e("Crypto", "Erro ao carregar a chave do KeyStore", e)
        null
    }
}

fun encrypt(data: String): String? {
    try {
        // 1. Carrega a chave secreta do AndroidKeyStore.
        val secretKey = getSecretKey() ?: throw Exception("Chave secreta não encontrada no KeyStore.")

        // 2. Obtém uma instância do Cipher com a transformação correta.
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // 3. Inicializa o Cipher em modo de criptografia.
        //    Para GCM, o IV é gerado de forma segura pelo próprio Cipher.
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // 4. Obtém o IV gerado, que precisaremos para a descriptografia.
        val iv = cipher.iv

        // 5. Criptografa os dados.
        val encryptedDataBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // 6. Combina o IV com os dados criptografados (IV primeiro).
        val combinedData = ByteArray(IV_SIZE_BYTES + encryptedDataBytes.size)
        System.arraycopy(iv, 0, combinedData, 0, IV_SIZE_BYTES)
        System.arraycopy(encryptedDataBytes, 0, combinedData, IV_SIZE_BYTES, encryptedDataBytes.size)

        // 7. Codifica o resultado em Base64 e retorna.
        return Base64.encodeToString(combinedData, Base64.DEFAULT)

    } catch (e: Exception) {
        Log.e("Crypto", "Erro ao criptografar os dados", e)
        return null
    }
}

fun decrypt(encryptedDataB64: String): String? {
    try {
        // 1. Decodifica os dados de Base64.
        val combinedData = Base64.decode(encryptedDataB64, Base64.DEFAULT)
        if (combinedData.size < IV_SIZE_BYTES) {
            throw IllegalArgumentException("Dados criptografados inválidos.")
        }

        // 2. Carrega a chave secreta do AndroidKeyStore.
        val secretKey = getSecretKey() ?: throw Exception("Chave secreta não encontrada no KeyStore.")

        // 3. Separa o IV dos dados criptografados.
        val iv = combinedData.copyOfRange(0, IV_SIZE_BYTES)
        val encryptedDataBytes = combinedData.copyOfRange(IV_SIZE_BYTES, combinedData.size)

        // 4. Cria a especificação dos parâmetros GCM usando o IV extraído.
        val gcmParameterSpec = GCMParameterSpec(128, iv)

        // 5. Obtém uma instância do Cipher e o inicializa em modo de descriptografia.
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        // 6. Descriptografa os dados e retorna como String.
        val decryptedDataBytes = cipher.doFinal(encryptedDataBytes)
        return String(decryptedDataBytes, Charsets.UTF_8)

    } catch (e: Exception) {
        Log.e("Crypto", "Erro ao descriptografar os dados", e)
        return null
    }
}

// Função para transformar uma string de cor em uma Color
fun hexToColor(hexString: String?): Color {
    return try {
        hexString?.removePrefix("0x")?.toULong(16)?.let { argb ->
            val alpha = ((argb shr 24) and 0xFFuL).toFloat() / 255f
            val red = ((argb shr 16) and 0xFFuL).toFloat() / 255f
            val green = ((argb shr 8) and 0xFFuL).toFloat() / 255f
            val blue = (argb and 0xFFuL).toFloat() / 255f
            Color(red, green, blue, alpha)
        } ?: AppColors.platinum
    } catch (e: Exception) {
        println("Erro ao converter cor hexadecimal '$hexString': ${e.message}")
        AppColors.platinum
    }
}

// Objeto para armazenar ID e nome de uma categoria
data class Categoria(
    val id: String,
    val nome: String,
    val cor: String
)

// Objeto para armazenar todas as informações das senhas cadastradas
data class SenhaData(
    val apelido: String = "",
    val login: String = "",
    val senha: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val id: String = ""
)

// Objeto para armezanar apenas nome e cor de uma categoria
data class CategoriaData(
    val nomeCategoria: String,
    val corCategoria: String
)

// Função para pegar as categorias do banco de dados e adicionar ao objeto Categoria
fun getCategorias(userId: String, context: Context, onResult: (List<Categoria>) -> Unit) {
    val db = Firebase.firestore

    db.collection("accounts")
        .document(userId)
        .collection("Categorias")
        .get()
        .addOnSuccessListener { result ->
            val categorias = result.documents
                .mapNotNull { doc ->
                    val nome = doc.getString("Nome")
                    val cor = doc.getString("Cor")
                    val id = doc.id
                    if (nome != null && cor != null) {
                        Categoria(id = id, nome = nome, cor = cor)
                    } else null
                }
            onResult(categorias)
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao buscar categorias no Firestore.", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
}

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsBold = FontFamily(Font(R.font.poppins_bold))
val poppinsMedium = FontFamily(Font(R.font.poppins_medium))

object PoppinsFonts {
    val regular = poppinsRegular
    val medium = poppinsMedium
    val bold = poppinsBold
}


