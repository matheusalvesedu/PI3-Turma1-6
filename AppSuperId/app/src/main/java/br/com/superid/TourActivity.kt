package br.com.superid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.ui.theme.SuperIDTheme

class TourActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                TourScreen()
            }
        }
    }
}

@Composable
fun TourScreen() {

    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
//        topBar = {
//            activityBackButton(activity)
//        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_superid_darkblue),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            val title = listOf(
                "Botão de cadastrar nova senha.",
                "Tela de cadastro de nova senha.",
                "Botão de adicionar/alterar categorias.",
                "Botão de adicionar nova categoria.",
                "Botão de alterar cor da categoria.",
                "Tela de alteração de cor da categoria.",
                "Botão de alterar categoria existente.",
                "Botão de excluir categoria.",
                "Botão de escanear QRCode.",
                "Botão de sair da conta."
            )
            val images = listOf(
                R.drawable.tour_1,
                R.drawable.tour_2,
                R.drawable.tour_3,
                R.drawable.tour_4,
                R.drawable.tour_5,
                R.drawable.tour_6,
                R.drawable.tour_7,
                R.drawable.tour_8,
                R.drawable.tour_9,
                R.drawable.tour_10
            )

            val pagerState = rememberPagerState(pageCount = {10})

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                HorizontalPager(
                    state = pagerState
                ) { page ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {

                        Image(
                            painter = painterResource(id = images[page]),
                            contentDescription = "Print da funcionalidade ${title[page]}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(600.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if(pagerState.currentPage != pagerState.pageCount - 1){
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (pagerState.currentPage == pagerState.pageCount - 1) 70.dp else 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                }

                if (pagerState.currentPage == pagerState.pageCount - 1) {
                    Button(
                        onClick = {
                            mudarTelaFinish(
                                context,
                                PrincipalScreenActivity::class.java
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(32.dp)
                            .height(50.dp)
                            .fillMaxWidth()

                    ) {
                        Text(
                            text = "Começar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

            }
        }
    }
}

