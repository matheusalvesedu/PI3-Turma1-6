package br.com.superid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.permissions.WithPermissionInActivity
import br.com.superid.ui.theme.ui.theme.SuperIDTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.superid.ui.theme.AppColors
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase





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

// Função de criptografia
fun aesEncryptWithKey(data: String): String{

    val key = SecretKeySpec(CryptoKey.getKey(), "AES")

    val cipher = Cipher.getInstance("AES")

    cipher.init(Cipher.ENCRYPT_MODE, key)
    val dataCripted = cipher.doFinal(data.toByteArray())

    return Base64.getEncoder().encodeToString(dataCripted)

}

// Função de descriptografia
fun aesDecryptWithKey(encryptedData: String): String{

    val dataBaseByteArray = Base64.getDecoder().decode(encryptedData)

    val key = SecretKeySpec(CryptoKey.getKey(), "AES")

    val cipher = Cipher.getInstance("AES")

    cipher.init(Cipher.DECRYPT_MODE,key)
    val dataDecrypted = cipher.doFinal(dataBaseByteArray)

    return String(dataDecrypted)
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


