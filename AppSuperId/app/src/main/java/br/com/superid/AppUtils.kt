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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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
            Text(text = texto,
                fontSize = 12.sp,
                fontFamily = PoppinsFonts.regular
            )
        },
        modifier = Modifier
            .width(300.dp)
            .padding(10.dp),
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = AppColors.gunmetal,
            unfocusedIndicatorColor = AppColors.platinum,
            containerColor = Color.Transparent,
            focusedLabelColor = AppColors.gunmetal,
            cursorColor = AppColors.gunmetal
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
            Text(text = texto,
                fontSize = 12.sp,
                fontFamily = PoppinsFonts.regular
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
            focusedIndicatorColor = AppColors.gunmetal,
            unfocusedIndicatorColor = AppColors.platinum,
            containerColor = Color.Transparent,
            focusedLabelColor = AppColors.gunmetal,
            cursorColor = AppColors.gunmetal
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
            containerColor = Color.Transparent,
            titleContentColor = Color.Transparent,
            navigationIconContentColor = AppColors.gunmetal
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
            containerColor = AppColors.white,
            titleContentColor = Color.Transparent,
            navigationIconContentColor = AppColors.gunmetal
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
fun aesEncryptWithKey(data: String): ByteArray{

    val byteData = data.toByteArray(Charsets.UTF_8)
    val keyString = System.getenv("AES_KEY") ?: error("AES_KEY não encontrado.")
    val keyBytes = keyString.toByteArray(Charsets.UTF_8)
    require(keyBytes.size == 32) { "AES_KEY deve ter 32 bytes para AES-256" }

    val secretKey = SecretKeySpec(keyBytes, "AES")
    val iv = IvParameterSpec(ByteArray(16))

    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

    return cipher.doFinal(byteData)
}

// Função de descriptografia
fun aesDescryptWithKey(encryptedData: ByteArray): String{
    val keyString = System.getenv("AES_KEY") ?: error("AES_KEY não encontrado.")
    val keyBytes = keyString.toByteArray(Charsets.UTF_8)
    require(keyBytes.size == 32) { "AES_KEY deve ter 32 bytes para AES-256" }

    val secretKey = SecretKeySpec(keyBytes, "AES")
    val iv = IvParameterSpec(ByteArray(16))

    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

    val decryptedBytes = cipher.doFinal(encryptedData)

    return String(decryptedBytes, Charsets.UTF_8)
}

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsBold = FontFamily(Font(R.font.poppins_bold))
val poppinsMedium = FontFamily(Font(R.font.poppins_medium))

object PoppinsFonts {
    val regular = poppinsRegular
    val medium = poppinsMedium
    val bold = poppinsBold
}