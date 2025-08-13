package com.example.openvideodatabase

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
import kotlinx.coroutines.delay
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box



/*
@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(), // occuperà tutta la pagina
        contentAlignment = Alignment.Center // centra il contenuto sia verticalmente che orizzontalmente
    ) {
        Text(
            text = "BENVENUTO",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Thin,
            fontSize = 36.sp // aumenta la dimensione del testo
        )
    }
}*/


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenVideoDatabaseTheme {
                WelcomeScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current

    // Questo effetto verrà eseguito quando il componente viene creato
    LaunchedEffect(Unit) {
        delay(2000) // Aspetta 2 secondi
        val intent = Intent(context, WelcomeAndSearchActivity::class.java).apply {
            putExtra("username", "user")
        }
        context.startActivity(intent)
        (context as? AppCompatActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center

    )

        {
        Text(
            text = "BENVENUTO",
            //modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.onBackground

        )
    }
}
