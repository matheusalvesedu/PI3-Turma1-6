package br.com.superid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.superid.ui.theme.AppColors
import io.github.cdimascio.dotenv.dotenv
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64



// Função para transicionar entre as telas
fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
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

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsBold = FontFamily(Font(R.font.poppins_bold))
val poppinsMedium = FontFamily(Font(R.font.poppins_medium))

object PoppinsFonts {
    val regular = poppinsRegular
    val medium = poppinsMedium
    val bold = poppinsBold
}


