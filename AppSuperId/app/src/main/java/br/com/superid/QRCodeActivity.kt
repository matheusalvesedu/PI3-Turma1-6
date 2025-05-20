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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.permissions.WithPermissionInActivity
import br.com.superid.ui.theme.ui.theme.SuperIDTheme
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
            }
        }
    }
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
            Spacer(modifier = Modifier.height(40.dp))

            // Título
            Text(
                text = "Escaneie o QR Code",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

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
                Spacer(modifier = Modifier.height(16.dp))
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
