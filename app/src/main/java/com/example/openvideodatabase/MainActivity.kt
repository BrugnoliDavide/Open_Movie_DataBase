package com.example.openvideodatabase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USERNAME = "username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val savedName = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_USERNAME, null)

        if (!savedName.isNullOrBlank()) {
            startActivity(
                Intent(this, WelcomeAndSearchActivity::class.java)
                    .putExtra("username", savedName)
            )
            finish()
            return
        }

        setContent {
            OpenVideoDatabaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Login(
                        modifier = Modifier.padding(innerPadding),
                        onLogin = { username ->
                            // Utilizzo della KTX extension per SharedPreferences
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
                                putString(KEY_USERNAME, username)
                            }

                            startActivity(
                                Intent(this@MainActivity, WelcomeAndSearchActivity::class.java)
                                    .putExtra("username", username)
                            )

                            finish()
                        }
                    )
                }
            }
        }
    }
}