package com.example.openvideodatabase



import androidx.activity.OnBackPressedCallback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import kotlinx.coroutines.delay
import retrofit2.Response
import android.widget.Toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
//barra di navigazione
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.core.app.ActivityOptionsCompat
import android.app.Activity




class WelcomeAndSearchActivity : ComponentActivity() {
    private var omdbApi: ApiOmdb? = null
    private val apiKey = "e68682b3"

    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000

    companion object {
        private const val TAG = "WelcomeSearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Activity creata")

        val username = intent.getStringExtra("username") ?: "Utente"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()

                if (currentTime - backPressedTime < backPressInterval) {
                    finishAffinity() // Chiude l'app
                } else {
                    Toast.makeText(
                        this@WelcomeAndSearchActivity,
                        "Premere di nuovo per uscire",
                        Toast.LENGTH_SHORT
                    ).show()
                    backPressedTime = currentTime
                }
            }
        })


        setupRetrofit()

        setContent {
            OpenVideoDatabaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WelcomeSearchScreen(
                        username = username,
                        omdbApi = omdbApi,
                        onSearchMovie = ::searchMovie,
                        apiKey = apiKey
                    )
                }
            }
        }
    }

    private fun setupRetrofit() {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.omdbapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            omdbApi = retrofit.create(ApiOmdb::class.java)
            Log.d(TAG, "Retrofit configurato")
        } catch (e: Exception) {
            Log.e(TAG, "Errore Retrofit: ${e.message}")
        }
    }

    private fun searchMovie(
        title: String,
        onResult: (String?) -> Unit
    ) {
        Log.d(TAG, "Ricerca film: $title")
        val api = omdbApi ?: run {
            runOnUiThread {
                Toast.makeText(this, "Errore di configurazione API", Toast.LENGTH_SHORT).show()
            }
            onResult(null)
            return
        }

        val call = api.searchByTitle(title, apiKey)
        call.enqueue(object : Callback<OmdbSearchResponse> {
            override fun onResponse(
                call: Call<OmdbSearchResponse>,
                response: Response<OmdbSearchResponse>
            ) {
                Log.d(TAG, "Risposta ricevuta: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val result = response.body()
                    val movieList = result?.Search

                    if (!movieList.isNullOrEmpty()) {
                        val imdbID = movieList[0].imdbID
                        Log.d(TAG, "Film trovato: $imdbID")
                        onResult(imdbID)
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@WelcomeAndSearchActivity,
                                "Nessun risultato trovato",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onResult(null)
                    }
                } else {
                    Log.e(TAG, "Errore risposta: ${response.code()}")
                    runOnUiThread {
                        Toast.makeText(
                            this@WelcomeAndSearchActivity,
                            "Errore nella ricerca",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<OmdbSearchResponse>, t: Throwable) {
                Log.e(TAG, "Errore rete: ${t.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@WelcomeAndSearchActivity,
                        "Errore di rete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                onResult(null)
            }
        })
    }
}

@Composable
fun WelcomeSearchScreen(
    username: String,
    omdbApi: ApiOmdb?,
    onSearchMovie: (String, (String?) -> Unit) -> Unit,
    //provare a rimuovere
    apiKey: String
) {
    var showWelcome by remember { mutableStateOf(true) }
    var searchTitle by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var retrofitStatus by remember { mutableStateOf(omdbApi != null) }
    val context = LocalContext.current

    // Aggiorna lo stato di Retrofit quando cambia l'API
    LaunchedEffect(omdbApi) {
        retrofitStatus = omdbApi != null
    }


    // Timer per passare dalla schermata welcome alla ricerca
    LaunchedEffect(showWelcome) {
        if (showWelcome) {
            delay(1)
            // Controlla se siamo ancora nella schermata di benvenuto
            if (showWelcome) {
                showWelcome = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Indicatore di stato Retrofit in alto
            //StatusIndicator(retrofitStatus)

            if (showWelcome) {
                WelcomeScreen(username)
            } else {
                SearchScreen(
                    searchTitle = searchTitle,
                    onSearchTitleChange = { searchTitle = it },
                    isSearching = isSearching,
                    onSearchClick = {
                        if (searchTitle.isNotBlank()) {
                            isSearching = true
                            onSearchMovie(searchTitle) { imdbID ->
                                isSearching = false
                                if (imdbID != null) {
                                    val intent = Intent(context, ShowDettailActivity::class.java)
                                    intent.putExtra("imdbID", imdbID)
                                    context.startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(context, "Inserisci un titolo", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }

        //vecchio bottone per passare a LikeFilmActivity
        /*Button(
            onClick = {
                val intent = Intent(context, LikeFilmActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(width = 140.dp, height = 44.dp),
        ) {
            Text(text = "LIKE FILM")
        }*/
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarItem(
                selected = true,
                onClick = {

                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text("Cerca", fontWeight = FontWeight.Bold) }
            )

            NavigationBarItem(
                selected = false,
                onClick = {
                        val intent = Intent(context, LikeFilmActivity::class.java)
                        context.startActivity(intent)
                    val activity = context as? Activity
                    if (activity != null) {
                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            activity,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        activity.startActivity(intent, options.toBundle())
                    } else {
                        // fallback generico se context non è Activity
                        context.startActivity(intent)
                    }




                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Preferiti",
                        tint = MaterialTheme.colorScheme.onSurface

                    )
                },
                label = { Text("Preferiti", fontWeight = FontWeight.Normal) }
            )
        }
    }
}


@Composable
fun StatusIndicator(retrofitStatus: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(
                color = if (retrofitStatus) Color.Green else Color.Red,
                radius = size.minDimension / 2
            )
        }
    }
}

@Composable
fun WelcomeScreen(username: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Benvenuto, $username!",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            //CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Caricamento...", fontSize = 16.sp)
        }
    }
}

@Composable
fun SearchScreen(
    searchTitle: String,
    onSearchTitleChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ricerca Film",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = searchTitle,
            onValueChange = onSearchTitleChange,
            label = { Text("Inserisci titolo del film") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isSearching
        )

        Button(
            onClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isSearching && searchTitle.isNotBlank()
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Cerca Film", fontSize = 16.sp)
            }
        }

        if (isSearching) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ricerca in corso...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}