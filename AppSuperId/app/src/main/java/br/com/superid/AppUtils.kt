package br.com.superid

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.AppColors

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

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsBold = FontFamily(Font(R.font.poppins_bold))
val poppinsMedium = FontFamily(Font(R.font.poppins_medium))

object PoppinsFonts {
    val regular = poppinsRegular
    val medium = poppinsMedium
    val bold = poppinsBold
}