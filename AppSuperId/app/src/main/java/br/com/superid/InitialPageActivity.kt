package br.com.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.PoppinsFonts
import br.com.superid.ui.theme.AppColors
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(color = AppColors.white),
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
            fontFamily = PoppinsFonts.bold,
            fontSize = 30.sp,
            lineHeight = 30.sp,
            color = AppColors.gunmetal,
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
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.gunmetal
            )
        ) {
            Text("Cadastre-se",
                fontFamily = PoppinsFonts.medium,
                fontSize = 30.sp,
                color = AppColors.platinum)
        }

        Spacer (modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                mudarTela(context, LoginActivity::class.java)
            },
            border = BorderStroke(2.dp, AppColors.gunmetal),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = AppColors.platinum),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(300.dp)
                .height(60.dp)
        ) {
            Text("Login",
                fontFamily = PoppinsFonts.medium,
                fontSize = 30.sp,
                color = AppColors.gunmetal
            )
        }
    }
}