//main
package com.example.openvideodatabase

//import com.example.openvideodatabase.WelcomeActivity
import android.os.Bundle

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import android.app.Activity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
import androidx.compose.foundation.layout.Column // Per disporre gli elementi verticalmente [6]
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button // Componente bottone [8]
import androidx.compose.runtime.getValue // Per la sintassi del delegato 'by' [15]
import androidx.compose.runtime.setValue // Per la sintassi del delegato 'by' [15]
import androidx.compose.ui.unit.dp // Per definire le unità di misura (Density-independent Pixels) [10]


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenVideoDatabaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Login(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



@Composable
fun Login(name: String, modifier: Modifier = Modifier) {
    var username by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BENVENUTO",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        TextField(
            value = username,
            onValueChange = { newText -> username = newText },
            label = { Text("inserisci il tuo nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        //inseriamo dello spazio per evitare che gli oggetti si intersichino
        Spacer(modifier = Modifier.height(16.dp))

        //bottone per il "login"

        Button(
            onClick = {
                if (username.isNotBlank()) {
                    val intent = Intent(context, WelcomeAndSearchActivity::class.java).apply {
                        putExtra("username", username)
                    }
                    context.startActivity(intent)

                    (context as? Activity)?.overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )

                } else {
                    Toast.makeText(context, "Per favore, inserisci il tuo nome!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}
    @Preview(showBackground = true)
    @Composable
    fun LoginPreview() {
        OpenVideoDatabaseTheme {
            Login("Android")
        }
    }