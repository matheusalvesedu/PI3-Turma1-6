package br.com.superid

import android.content.Context
import android.content.Intent

// Função para transicionar entre as telas
fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}