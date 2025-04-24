package br.com.superid

import android.content.Context
import android.content.Intent
import org.mindrot.jbcrypt.BCrypt

// Função para transicionar entre as telas
fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}

fun hashPassword(password: String, cost: Int = 10): String {
    val salt = BCrypt.gensalt(cost)
    return BCrypt.hashpw(password, salt)
}