package br.com.superid

import android.content.Context
import android.content.Intent
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

// Função para transicionar entre as telas
fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsBold = FontFamily(Font(R.font.poppins_bold))
val poppinsMedium = FontFamily(Font(R.font.poppins_medium))

object PoppinsFonts {
    val regular = poppinsRegular
    val medium = poppinsMedium
    val bold = poppinsBold
}