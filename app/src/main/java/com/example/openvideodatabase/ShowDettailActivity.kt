package com.example.openvideodatabase

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.ArrowBack

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.Review
import com.example.openvideodatabase.data.ReviewRepository

import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.combinedClickable
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi


//per sfondo
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
//import com.google.accompanist.systemuicontroller.rememberSystemUiController

//necessarie per bottone dinamico
import kotlinx.coroutines.flow.flowOf
import androidx.compose.runtime.collectAsState


class ShowDettailActivity : ComponentActivity() {

    private var omdbApi: ApiOmdb? = null
    private val apiKey = "e68682b3"
    private lateinit var reviewRepository: ReviewRepository


    companion object {
        private const val TAG = "ShowDettailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        val imdbID = intent.getStringExtra("imdbID") ?: ""
        Log.d(TAG, "IMDb ID ricevuto: $imdbID")


        setupRetrofit()

        setContent {
            OpenVideoDatabaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MovieDetailsScreen(
                        imdbID = imdbID,
                        omdbApi = omdbApi,
                        apiKey = apiKey,
                        reviewRepository = reviewRepository
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
@OptIn(ExperimentalFoundationApi::class)
fun MovieDetailsScreen(
    imdbID: String,
    omdbApi: ApiOmdb?,
    apiKey: String,
    reviewRepository: ReviewRepository
) {
    var movieDetails by remember { mutableStateOf<OmdbMovieDetails?>(null) }
    //var lastReview by remember { mutableStateOf<Review?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val reviewsFlow = remember(movieDetails) {
        movieDetails?.Title?.let { title ->
            reviewRepository.getReviewsByTitleFlow(title)
        } ?: flowOf(emptyList())
    }

    val reviews by reviewsFlow.collectAsState(initial = emptyList())
    val lastReview = reviews.firstOrNull()




    LaunchedEffect(imdbID) {
        if (omdbApi != null && imdbID.isNotEmpty()) {
            try {
                val call = omdbApi.getMovieDetails(imdbID, apiKey)
                call.enqueue(object : Callback<OmdbMovieDetails> {
                    override fun onResponse(
                        call: Call<OmdbMovieDetails>,
                        response: Response<OmdbMovieDetails>
                    ) {
                        isLoading = false
                        if (response.isSuccessful) {
                            val details = response.body()
                            if (details?.Response == "True") {
                                movieDetails = details
                                errorMessage = null
                            } else {
                                errorMessage = details?.Error ?: "Film non trovato"
                            }
                        } else {
                            errorMessage = when (response.code()) {
                                401 -> "Errore di autenticazione (API key non valida)"
                                404 -> "Risorsa non trovata"
                                else -> "Errore nella risposta: ${response.code()}"
                            }
                        }
                    }

                    override fun onFailure(call: Call<OmdbMovieDetails>, t: Throwable) {
                        isLoading = false
                        errorMessage = "Errore di rete: ${t.message}"
                    }
                })
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Errore nella chiamata API: ${e.message}"
            }
        } else {
            isLoading = false
            errorMessage = when {
                omdbApi == null -> "Errore di configurazione API"
                imdbID.isEmpty() -> "ID film non valido"
                else -> "Errore sconosciuto"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Poster a pieno schermo in alto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    ) {
                        AsyncImage(
                            model = movieDetails?.Poster,
                            contentDescription = "Poster Film",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Overlay scuro
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )

                        // Sfumatura
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 300f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )


                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = movieDetails?.Title ?: "Titolo non disponibile",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${movieDetails?.Year ?: ""} • ${movieDetails?.Genre ?: ""}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )






                        }
                    }

                    // Dettagli completi sotto
                    Spacer(modifier = Modifier.height(16.dp))
                    MovieDetailsContent(movieDetails!!)

                    lastReview?.let { review ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.padding(30.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(24.dp))
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = {
                                        val intent = Intent(context, EditReviewActivity::class.java).apply {
                                            putExtra("REVIEW_ID", review.id)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Valutazione: ${review.rating}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                            Text(
                                text = "Prima visualizzazione: ${review.firstViewed ?: "N/D"}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                            if (!review.comment.isNullOrBlank()) {
                                Text(
                                    text = "Commento: ${review.comment}",
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }



        IconButton(
            onClick = {
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Torna indietro",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        // Pulsante preferiti
        Button(
            onClick = {
                movieDetails?.let { details ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val exists = reviewRepository.existsByTitle(details.Title ?: "")
                        //val existingReview = reviewRepository.getLastReviewByTitle(details.Title ?: "")

                        if (exists) {
                            CoroutineScope(Dispatchers.Main).launch {

                                Toast.makeText(
                                    context,
                                    "Film già aggiunto ai preferiti",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val review = Review(
                                externalId = details.imdbID ?: "ID sconosciuto",
                                title = details.Title ?: "Titolo sconosciuto",
                                rating = 0f
                            )
                            reviewRepository.insert(review)
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context,
                                    "Film aggiunto ai preferiti",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(75.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Aggiungi ai preferiti",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun MovieDetailsContent(details: OmdbMovieDetails) {
        Column(modifier = Modifier.padding(16.dp)) {

        DetailRow("Durata", details.Runtime)

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

