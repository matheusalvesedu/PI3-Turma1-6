package br.com.superid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SuperIDApp()
            }
        }
    }
}

fun mudarTela(context: android.content.Context, destination: Class<*>){
    val intent = Intent(context,destination)
    context.startActivity(intent)
}

@Preview
@Composable
fun SuperIDApp(){
    SuperID(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun SuperID(modifier: Modifier = Modifier){
    val context = LocalContext.current
   Column(
        modifier = modifier,
       horizontalAlignment = Alignment.CenterHorizontally
   ) {
       Text(text = "Bem Vindo ao SuperID!!",
           style = MaterialTheme.typography.headlineLarge.copy(
               fontWeight = FontWeight.Bold,
               color = MaterialTheme.colorScheme.primary
           ),
           textAlign = TextAlign.Center,
           modifier = Modifier
               .padding(10.dp)
       )
       Spacer(modifier = Modifier.size(10.dp))
       Button(
           onClick = { mudarTela(context, LoginActivity::class.java)}
       ) {Text("Login") }
       Spacer(modifier = Modifier.size(10.dp))

       Button(
           onClick = { /*mudarTela(/*TODO*/)*/ }
       ){
            Text("Cadastrar")
       }
   }

}