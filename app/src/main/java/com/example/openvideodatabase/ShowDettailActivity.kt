package com.example.openvideodatabase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme

class ShowDettailActivity : ComponentActivity() {
    private var omdbApi: ApiOmdb? = null
    private val apiKey = "e68682b3"

    companion object {
        private const val TAG = "ShowDettailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imdbID = intent.getStringExtra("imdbID") ?: ""
        Log.d(TAG, "IMDb ID ricevuto: $imdbID")

        setupRetrofit()

        setContent {
            OpenVideoDatabaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MovieDetailsScreen(
                        imdbID = imdbID,
                        omdbApi = omdbApi,
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
            Log.d(TAG, "Retrofit configurato per dettagli")
        } catch (e: Exception) {
            Log.e(TAG, "Errore Retrofit: ${e.message}")
        }
    }
}
@Composable
fun MovieDetailsScreen(
    imdbID: String,
    omdbApi: ApiOmdb?,
    apiKey: String
) {
    var movieDetails: OmdbMovieDetails? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage: String? by remember { mutableStateOf(null) }

    LaunchedEffect(imdbID) {
        if (omdbApi != null && imdbID.isNotEmpty()) {
            Log.d(
                "MovieDetails",
                "Chiamata API per ID: $imdbID con key: ${apiKey.take(3)}..."
            ) // Log parziale della key

            try {
                val call = omdbApi.getMovieDetails(imdbID, apiKey)
                call.enqueue(object : Callback<OmdbMovieDetails> {
                    override fun onResponse(
                        call: Call<OmdbMovieDetails>,
                        response: Response<OmdbMovieDetails>
                    ) {
                        isLoading = false
                        Log.d("MovieDetails", "Risposta ricevuta: ${response.code()}")

                        if (response.isSuccessful) {
                            val details = response.body()
                            Log.d(
                                "MovieDetails",
                                "Response: ${details?.Response}, Error: ${details?.Error}"
                            )

                            if (details?.Response == "True") {
                                movieDetails = details
                            } else {
                                errorMessage = details?.Error ?: "Film non trovato"
                                Log.e("MovieDetails", "Errore API: $errorMessage")
                            }
                        } else {
                            errorMessage = when (response.code()) {
                                401 -> "Errore di autenticazione (API key non valida)"
                                404 -> "Risorsa non trovata"
                                else -> "Errore nella risposta: ${response.code()}"
                            }
                            Log.e("MovieDetails", errorMessage ?: "Errore sconosciuto")
                        }
                    }

                    override fun onFailure(call: Call<OmdbMovieDetails>, t: Throwable) {
                        isLoading = false
                        errorMessage = "Errore di rete: ${t.message}"
                        Log.e("MovieDetails", "Errore di rete", t)
                    }
                })
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Errore nella chiamata API: ${e.message}"
                Log.e("MovieDetails", "Eccezione durante la chiamata API", e)
            }
        } else {
            isLoading = false
            errorMessage = when {
                omdbApi == null -> "Errore di configurazione API"
                imdbID.isEmpty() -> "ID film non valido"
                else -> "Errore sconosciuto"
            }
            Log.e("MovieDetails", errorMessage ?: "Errore sconosciuto")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Caricamento dettagli film...")
                }
            }

            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Errore: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }
            }

            movieDetails != null -> {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    MovieDetailsContent(movieDetails!!)
                }
            }
        }
    }
}


    @Composable
    fun MovieDetailsContent(details: OmdbMovieDetails) {
        Column {
            Text(
                text = details.Title ?: "Titolo non disponibile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DetailRow("Anno", details.Year)
            DetailRow("Durata", details.Runtime)
            DetailRow("Genere", details.Genre)
            DetailRow("Regista", details.Director)
            DetailRow("Attori", details.Actors)
            DetailRow("Trama", details.Plot)
            DetailRow("Valutazione IMDb", details.imdbRating)
            DetailRow("Voti IMDb", details.imdbVotes)
            DetailRow("Premi", details.Awards)
            DetailRow("Paese", details.Country)
            DetailRow("Lingua", details.Language)
        }
    }

    @Composable
    fun DetailRow(label: String, value: String?) {
        if (!value.isNullOrBlank() && value != "N/A") {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

