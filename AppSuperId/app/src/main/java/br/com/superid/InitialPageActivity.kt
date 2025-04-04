package br.com.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme


class InitialPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                TelaInicialApp()
            }
        }
    }
}

@Preview
@Composable
fun TelaInicialApp(){
    TelaInicial(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(modifier: Modifier = Modifier){
    var context = LocalContext.current
    var presses by remember { mutableStateOf(0)}
    Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            //TODO: Substituir texto pelo ícone do app
                            Text(text = "Super ID")
                        }
                    },
                    colors = topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },

        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .background(color = Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Text(text = "O gerenciador\nde senhas\nmais seguro\ndo mercado.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .padding(30.dp)
                )

                Spacer(modifier = Modifier.height(150.dp))

                Button(
                    onClick = {
                        mudarTela(context, SignUpActivity::class.java)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(300.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Criar conta", fontSize = 20.sp)
                }

                OutlinedButton(
                    onClick = {
                        //TODO: Mudar a tela para o Login
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(300.dp)
                        .height(60.dp)
                ) {
                    Text("Já tenho uma conta",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }

fun mudarTela(context: Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}