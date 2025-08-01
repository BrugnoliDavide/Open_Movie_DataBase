/*package com.example.openvideodatabase

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("username") ?: "Utente"

        setContent {
            OpenVideoDatabaseTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Benvenuto, $username!")
                }
            }
        }

        // Alternativa con Thread
        Thread {
            Thread.sleep(3000)
            runOnUiThread {
                val intent = Intent(this@WelcomeActivity, SearchMenuActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}
*/