package br.com.superid

import android.content.Context
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme


class InitialPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userSharedPreferences = getSharedPreferences("user_prefs",Context.MODE_PRIVATE)
        val loggedIn = userSharedPreferences.getBoolean("is_logged",false)

        if(!loggedIn){
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
        }else{
            mudarTela(this, PrincipalScreenActivity::class.java)
            finish()
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
                .weight(0.3f)
                .padding(top = 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_superid_darkblue),
                contentDescription = "Logo do Super ID",
                modifier = Modifier
                    .size(100.dp)
            )
        }

        Text(text = "O gerenciador\nde senhas\nmais seguro\ndo mercado.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .padding(30.dp)
        )

        Button(
            onClick = {
                mudarTela(context, SignUpActivity::class.java)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(40.dp)
                .weight(0.1f)
                .padding(bottom = 10.dp, top = 10.dp)
        ) {
            Text("Cadastre-se",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                ))
        }

        OutlinedButton(
            onClick = {
                mudarTela(context, LoginActivity::class.java)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(40.dp)
                .weight(0.1f)
                .padding(bottom = 20.dp)
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