package br.com.superid

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.permissions.WithPermissionInActivity
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Check if email is verified
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser?.isEmailVerified == true) {
                        var showPasswordDialog by remember { mutableStateOf(true) }
                        var isPasswordVerified by remember { mutableStateOf(false) }

                        if (showPasswordDialog && !isPasswordVerified) {
                            PasswordVerificationDialog(
                                onPasswordVerified = {
                                    isPasswordVerified = true
                                    showPasswordDialog = false
                                },
                                onDismiss = { finish() }
                            )
                        }

                        if (isPasswordVerified) {
                            WithPermissionInActivity(
                                modifier = Modifier.padding(innerPadding),
                                permission = Manifest.permission.CAMERA,
                                activity = this
                            ) {
                                val context = LocalContext.current
                                QrScannerScreen(
                                    onQrCodeScanned = { qrCode ->
                                        try {
                                            val uri = qrCode.toUri()
                                            val loginToken = uri.getQueryParameter("token") ?: uri.toString()
                                            if (loginToken.isNotBlank()) {
                                                handleLoginToken(context = context, loginToken = loginToken)
                                            } else {
                                                Toast.makeText(context, "QR Code inválido", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(context, "Erro ao processar QR Code", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onClose = { finish() }
                                )
                            }
                        }
                    } else {
                        // Show message and finish activity if email is not verified
                        LaunchedEffect(Unit) {
                            Toast.makeText(
                                this@QRCodeActivity,
                                "Verifique seu e-mail para usar login sem senha",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVerificationDialog(
    onPasswordVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Verificação de Senha",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Digite sua senha") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser
                    
                    if (currentUser != null) {
                        val email = currentUser.email
                        if (email != null) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onPasswordVerified()
                                    } else {
                                        errorMessage = "Senha incorreta"
                                    }
                                }
                        }
                    }
                },
                enabled = password.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verificar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun QrScannerScreen(
    onQrCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val activity = LocalActivity.current

    // Controle para evitar múltiplos escaneamentos
    var hasScanned by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        startCameraWithAnalyzer(
            context = context,
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            onQrCodeScanned = { qrCode ->
                if (!hasScanned) {
                    hasScanned = true
                    onQrCodeScanned(qrCode)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { activityBackButton(activity) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.05f))  // 5% of screen height

            // Título
            Text(
                text = "Escaneie o QR Code",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.fillMaxHeight(0.02f))  // 2% of screen height

            // Visualização da câmera
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.fillMaxHeight(0.02f))  // 2% of screen height

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Aponte para o QR Code",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.05f))  // 2% of screen height
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun startCameraWithAnalyzer(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    onQrCodeScanned: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val barcodeScanner = BarcodeScanning.getClient()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { qrCode ->
                            Log.i("QRCODE", qrCode)
                            onQrCodeScanned(qrCode)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun handleLoginToken(context: Context, loginToken: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        return
    }

    val uid = currentUser.uid
    val firestore = FirebaseFirestore.getInstance()
    val loginDocRef = firestore.collection("login").document(loginToken)

    loginDocRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val updates = mapOf(
                    "user" to uid,
                    "loginAt" to FieldValue.serverTimestamp()
                )
                loginDocRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Login confirmado!", Toast.LENGTH_SHORT).show()
                        (context as? Activity)?.finish() // Fecha a activity
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao confirmar login", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Token não encontrado ou expirado", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro ao buscar token", Toast.LENGTH_SHORT).show()
        }
}
