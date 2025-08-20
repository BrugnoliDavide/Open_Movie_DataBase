package com.example.openvideodatabase

// Barra di navigazione + preferiti

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import coil.compose.AsyncImage
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.Review
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

class LikeFilmActivity : ComponentActivity() {

    private lateinit var reviewRepository: ReviewRepository

    private var omdbApi: ApiOmdb? = null
    private val apiKey = "e68682b3"

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        omdbApi = retrofit.create(ApiOmdb::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LikeFilmActivity, WelcomeAndSearchActivity::class.java)
                intent.putExtra("from_other_activity", true) // Aggiungi questa linea
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })

        setupRetrofit()

        // Inizializzo il repository
        val db = AppDatabase.getInstance(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        setContent {
            OpenVideoDatabaseTheme {
                FavoritesScreen(
                    reviewRepository = reviewRepository,
                    onBack = { finish() }
                )
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun FavoritesScreen(reviewRepository: ReviewRepository, onBack: () -> Unit) {
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var allFavorites by remember { mutableStateOf(listOf<Review>()) }
        var filteredFavorites by remember { mutableStateOf(listOf<Review>()) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                val favorites = reviewRepository.getAllReviews()
                withContext(Dispatchers.Main) {
                    allFavorites = favorites
                    filteredFavorites = favorites
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Film Preferiti") }
                    // il tasto indietro non serve con la NavBar, lo lasciamo commentato
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val intent = Intent(context, WelcomeAndSearchActivity::class.java)
                            intent.putExtra("from_other_activity", true) // AGGIUNTA: parametro per evitare schermata di benvenuto
                            val activity = context as? Activity
                            if (activity != null) {
                                val options = ActivityOptionsCompat.makeCustomAnimation(
                                    activity,
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out
                                )
                                context.startActivity(intent, options.toBundle())
                            } else {
                                context.startActivity(intent)
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Cerca") },
                        label = { Text("Cerca", fontWeight = FontWeight.Normal) }
                    )

                    NavigationBarItem(
                        selected = true, // schermata corrente
                        onClick = { /* no-op */ },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Preferiti",
                                tint = Color.Red
                            )
                        },
                        label = { Text("Preferiti", fontWeight = FontWeight.ExtraBold) }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        filteredFavorites = if (query.text.isEmpty()) {
                            allFavorites
                        } else {
                            allFavorites.filter { it.title.contains(query.text, ignoreCase = true) }
                        }
                    },
                    label = { Text("Cerca...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(
                        items = filteredFavorites,
                        key = { it.id }
                    ) { review ->
                        FavoriteItem(
                            review = review,
                            onShowDetails = { imdbId ->
                                val intent = Intent(context, ShowDettailActivity::class.java).apply {
                                    putExtra("imdbID", imdbId)
                                }
                                context.startActivity(intent)
                            },
                            onDelete = { reviewToDelete ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    reviewRepository.delete(reviewToDelete)
                                    val updated = reviewRepository.getAllReviews()
                                    withContext(Dispatchers.Main) {
                                        allFavorites = updated
                                        filteredFavorites = if (searchQuery.text.isEmpty()) updated
                                        else updated.filter {
                                            it.title.contains(searchQuery.text, ignoreCase = true)
                                        }
                                    }
                                }
                            },
                            onRate = { reviewToRate: Review, newRating: Float ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val updatedReview = reviewToRate.copy(rating = newRating)
                                    reviewRepository.update(updatedReview)
                                    val updatedList = reviewRepository.getAllReviews()
                                    withContext(Dispatchers.Main) {
                                        allFavorites = updatedList
                                        filteredFavorites =
                                            if (searchQuery.text.isEmpty()) updatedList
                                            else updatedList.filter {
                                                it.title.contains(searchQuery.text, ignoreCase = true)
                                            }
                                    }
                                }
                            },
                            onSetFirstViewed = { reviewToUpdate: Review, newDate: Date ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val updatedReview = reviewToUpdate.copy(firstViewed = newDate)
                                    reviewRepository.update(updatedReview)
                                    val updatedList = reviewRepository.getAllReviews()
                                    withContext(Dispatchers.Main) {
                                        allFavorites = updatedList
                                        filteredFavorites =
                                            if (searchQuery.text.isEmpty()) updatedList
                                            else updatedList.filter {
                                                it.title.contains(searchQuery.text, ignoreCase = true)
                                            }
                                    }
                                }
                            },
                            omdbApi = omdbApi,
                            apiKey = apiKey
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun FavoriteItem(
        review: Review,
        onShowDetails: (String) -> Unit,
        onDelete: (Review) -> Unit,
        onRate: (Review, Float) -> Unit,
        onSetFirstViewed: (Review, Date) -> Unit,
        omdbApi: ApiOmdb?,
        apiKey: String
    ) {
        var expanded by remember { mutableStateOf(false) }
        var posterUrl by remember { mutableStateOf<String?>(null) }
        var year by remember { mutableStateOf<String?>(null) }

        // Dialog valutazione
        var showRatingDialog by remember { mutableStateOf(false) }
        var ratingInput by remember { mutableStateOf("") }

        // Dialog data prima visione
        var showDateDialog by remember { mutableStateOf(false) }
        var dateInput by remember { mutableStateOf("") }
        val dateFormat = remember {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply { isLenient = false }
        }
        val context = LocalContext.current

        // Scarico poster/anno solo se ho un imdbID valido
        LaunchedEffect(review.externalId) {
            val imdbId = review.externalId
            if (omdbApi != null && !imdbId.isNullOrBlank()) {
                try {
                    val response = withContext(Dispatchers.IO) {
                        omdbApi.getMovieDetails(imdbId, apiKey).execute()
                    }
                    if (response.isSuccessful) {
                        posterUrl = response.body()?.Poster
                        year = response.body()?.Year
                    } else {
                        posterUrl = null; year = null
                    }
                } catch (_: Exception) {
                    posterUrl = null; year = null
                }
            } else {
                posterUrl = null; year = null
            }
        }

        // Card (click = mostra dettagli, long-click = menu)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = {
                        review.externalId?.let { onShowDetails(it) }
                    },
                    onLongClick = { expanded = true }
                ),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!posterUrl.isNullOrEmpty() && posterUrl != "N/A") {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = "Poster",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (!year.isNullOrEmpty()) {
                            Text(
                                text = year!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!review.comment.isNullOrEmpty()) {
                        Text(
                            text = review.comment!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Mostra dettagli") },
                    onClick = {
                        expanded = false
                        review.externalId?.let { onShowDetails(it) }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Elimina") },
                    onClick = {
                        expanded = false
                        onDelete(review)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Inserisci valutazione") },
                    onClick = {
                        expanded = false
                        showRatingDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Imposta prima visualizzazione") },
                    onClick = {
                        expanded = false
                        showDateDialog = true
                        dateInput = ""
                    }
                )
            }
        }

        // Dialog valutazione (0..10)
        if (showRatingDialog) {
            AlertDialog(
                onDismissRequest = { showRatingDialog = false },
                title = { Text(text = "Inserisci valutazione") },
                text = {
                    OutlinedTextField(
                        value = ratingInput,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d{0,2}(?:\\.\\d{0,1})?\$"))) {
                                val v = newValue.toFloatOrNull()
                                if (v == null || v in 0f..10f) ratingInput = newValue
                            }
                        },
                        label = { Text("Valutazione (0-10)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val rating = ratingInput.toFloatOrNull()
                        if (rating != null) {
                            onRate(review, rating)
                            showRatingDialog = false
                            ratingInput = ""
                        } else {
                            Toast.makeText(context, "Valore non valido", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Conferma") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRatingDialog = false
                        ratingInput = ""
                    }) { Text("Annulla") }
                }
            )
        }

        // Dialog data (dd-MM-yyyy coerente con label e parser)
        if (showDateDialog) {
            AlertDialog(
                onDismissRequest = { showDateDialog = false },
                title = { Text(text = "Inserisci data prima visualizzazione") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { newValue ->
                                // dd-MM-yyyy (con - opzionali durante la digitazione)
                                if (newValue.matches(Regex("^\\d{0,2}-?\\d{0,2}-?\\d{0,4}\$"))) {
                                    dateInput = newValue
                                }
                            },
                            label = { Text("Data (dd-MM-yyyy)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { dateInput = dateFormat.format(Date()) }) {
                            Text("Imposta data di oggi")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            val parsed = dateFormat.parse(dateInput)
                            if (parsed != null) {
                                onSetFirstViewed(review, parsed)
                                showDateDialog = false
                                dateInput = ""
                            } else {
                                Toast.makeText(context, "Data non valida", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "Data non valida", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Conferma") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDateDialog = false
                        dateInput = ""
                    }) { Text("Annulla") }
                }
            )
        }
    }
}