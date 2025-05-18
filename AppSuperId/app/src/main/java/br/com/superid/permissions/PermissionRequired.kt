package br.com.superid.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import br.com.superid.ui.theme.AppColors
import br.com.superid.ScreenBackButton
import br.com.superid.PoppinsFonts
import br.com.superid.activityBackButton

@Composable
fun PermissionScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    permission: String,
    onPermissionGranted: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ScreenBackButton(navController = navController, context = context)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Ícone do cadeado
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Black
            )

            Text(
                "Super ID",
                fontFamily = PoppinsFonts.bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto de explicação
            Text(
                text = "Para usar o login com QRCode é necessário aceitar as permissões",
                fontFamily = PoppinsFonts.regular,
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botão permitir
            Button(
                onClick = { launcher.launch(permission) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.black,
                    contentColor = Color.White
                )
            ) {
                Text("Permitir", fontFamily = PoppinsFonts.medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão cancelar
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(
                    text = "Cancelar",
                    fontFamily = PoppinsFonts.medium
                )
            }
        }
    }
}


@Composable
fun WithPermissionInActivity(
    modifier: Modifier = Modifier,
    permission: String,
    activity: ComponentActivity, // Passamos a activity diretamente
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    if (!permissionGranted) {
        // Versão simplificada da tela de permissão para uso em Activities
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Ícone do cadeado
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Black
                )

                Text(
                    "Super ID",
                    fontFamily = PoppinsFonts.bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Texto de explicação
                Text(
                    text = "Para usar o login com QRCode é necessário aceitar as permissões",
                    fontFamily = PoppinsFonts.regular,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Launcher para solicitar permissão
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        permissionGranted = true
                    }
                }

                // Botão permitir
                Button(
                    onClick = { launcher.launch(permission) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Permitir", fontFamily = PoppinsFonts.medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão cancelar
                Button(
                    onClick = { activity.finish() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = "Cancelar",
                        fontFamily = PoppinsFonts.medium
                    )
                }
            }
        }
    } else {
        Surface(modifier = modifier) {
            content()
        }
    }
}