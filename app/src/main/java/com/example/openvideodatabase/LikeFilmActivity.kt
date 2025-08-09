package com.example.openvideodatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme

class LikeFilmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenVideoDatabaseTheme {
                // Qui puoi definire il contenuto composable della LikeFilmActivity
                // Per ora semplice testo
                androidx.compose.material3.Text(text = "Schermata Like Film")
            }
        }
    }
}
