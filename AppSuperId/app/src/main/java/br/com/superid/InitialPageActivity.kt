package br.com.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.AppColors
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TelaInicialApp()
                }
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

@Composable
fun TelaInicial(modifier: Modifier = Modifier){
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_superid_darkblue),
                contentDescription = "Logo do Super ID",
                modifier = Modifier
                    .size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))

        Text(text = "O gerenciador\nde senhas\nmais seguro\ndo mercado.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        )

        Spacer(modifier = Modifier.height(180.dp))

        Button(
            onClick = {
                mudarTela(context, SignUpActivity::class.java)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(60.dp)
        ) {
            Text("Cadastre-se",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                ))
        }

        Spacer (modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                mudarTela(context, LoginActivity::class.java)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(60.dp)
        ) {
            Text("Login",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}