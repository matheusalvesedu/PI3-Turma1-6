package br.com.superid.permissions

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.PoppinsFonts
import br.com.superid.R
import br.com.superid.activityBackButton


@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    permission: String,
    activity: ComponentActivity,
    onPermissionGranted: () -> Unit = {}
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize() ,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { activityBackButton(activity) }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Super ID
            Icon(
                painter = painterResource(R.drawable.logo_superid_darkblue),
                contentDescription = "Logo Super ID",
                modifier = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Para usar o login com QRCode é necessário aceitar as permissões",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(innerPadding)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botão permitir
            Button(
                onClick = { launcher.launch(permission) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary ,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text("Permitir", fontFamily = PoppinsFonts.medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { activity.finish() },
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
    activity: ComponentActivity,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    if (!permissionGranted) {
        PermissionScreen(
            modifier = modifier,
            permission = permission,
            activity = activity,
            onPermissionGranted = {
                permissionGranted = true
            }
        )
    } else {
        Surface(modifier = modifier) {
            content()
        }
    }
}