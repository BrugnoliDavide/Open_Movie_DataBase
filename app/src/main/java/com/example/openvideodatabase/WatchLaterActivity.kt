package com.example.openvideodatabase

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import coil.compose.AsyncImage
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.WatchLaterRepo
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.WatchLaterMovie
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.saveable.rememberSaveable

class WatchLaterActivity : ComponentActivity() {

    private lateinit var watchLaterRepo: WatchLaterRepo
    private lateinit var reviewRepository: ReviewRepository

    // OMDb
    private var omdbApi: ApiOmdb? = null
    private val apiKey = "e68682b3"

    private fun setupRetrofit() {
        omdbApi = Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiOmdb::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Back personalizzato
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@WatchLaterActivity, WelcomeAndSearchActivity::class.java)
                intent.putExtra("from_other_activity", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })

        setupRetrofit()

        val db = AppDatabase.getInstance(applicationContext)
        watchLaterRepo = WatchLaterRepo(db.watchLaterDao())
        reviewRepository = ReviewRepository(db.reviewDao())

        setContent {
            OpenVideoDatabaseTheme {
                WatchLaterScreen(
                    watchLaterRepo = watchLaterRepo,
                    reviewRepo = reviewRepository,
                    omdbApi = omdbApi,
                    apiKey = apiKey,
                    onBack = { finish() }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WatchLaterScreen(
    watchLaterRepo: WatchLaterRepo,
    reviewRepo: ReviewRepository,
    omdbApi: ApiOmdb?,
    apiKey: String,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var allWatchLater by remember { mutableStateOf(listOf<WatchLaterMovie>()) }

    // Stato ordinamento
    var sortType by rememberSaveable { mutableStateOf("cronologico") }          // "Id" | "Titolo"
    var sortAsc by rememberSaveable { mutableStateOf(false) }          // default: DESC per Id
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Carica la lista una sola volta (o richiamala dopo mutate DB)
    LaunchedEffect(Unit) {
        val updated = withContext(Dispatchers.IO) { watchLaterRepo.getAllMovies() }
        allWatchLater = updated
    }

    // Lista visibile = filtro + ordinamento (ricaclolo reattivo)
    val visibleMovies by remember(searchQuery, sortType, sortAsc, allWatchLater) {
        derivedStateOf {
            val base = if (searchQuery.isBlank()) {
                allWatchLater
            } else {
                allWatchLater.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
            val sorted = when (sortType) {
                "Titolo" -> base.sortedBy { it.title.lowercase() }
                else -> base.sortedBy { it.id } // "Id"
            }
            if (sortAsc) sorted else sorted.asReversed()
        }
    }

    fun refresh() = coroutineScope.launch {
        val updated = withContext(Dispatchers.IO) { watchLaterRepo.getAllMovies() }
        allWatchLater = updated
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guarda più tardi") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ordina per:",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box {
                            Button(
                                onClick = { sortMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(sortType)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("cronologico") },
                                    onClick = {
                                        sortType = "Id"
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Titolo") },
                                    onClick = {
                                        sortType = "Titolo"
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { sortAsc = !sortAsc }) {
                            Icon(
                                imageVector = if (sortAsc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Cambia direzione"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        val intent = Intent(context, WelcomeAndSearchActivity::class.java)
                            .putExtra("from_other_activity", true)
                        (context as? Activity)?.let { act ->
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                act, android.R.anim.fade_in, android.R.anim.fade_out
                            )
                            context.startActivity(intent, options.toBundle())
                        } ?: context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Cerca") },
                    label = { Text("Cerca") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        val intent = Intent(context, LikeFilmActivity::class.java)
                            .putExtra("from_other_activity", true)
                        (context as? Activity)?.let { act ->
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                act, android.R.anim.fade_in, android.R.anim.fade_out
                            )
                            context.startActivity(intent, options.toBundle())
                        } ?: context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Preferiti") },
                    label = { Text("Preferiti") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* corrente */ },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Guarda più tardi") },
                    label = { Text("Guarda più tardi", fontWeight = FontWeight.Bold) }
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
                onValueChange = { searchQuery = it },
                label = { Text("Cerca...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (visibleMovies.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "Lista vuota" else "Nessun risultato per '$searchQuery'",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn {
                    items(items = visibleMovies, key = { it.id }) { movie ->
                        WatchLaterItem(
                            movie = movie,
                            omdbApi = omdbApi,
                            apiKey = apiKey,
                            onMovieClick = { selectedMovie ->
                                val intent = Intent(context, ShowDettailActivity::class.java)
                                    .putExtra("imdbID", selectedMovie.externalId)
                                    .putExtra("title", selectedMovie.title)
                                context.startActivity(intent)
                            },

                            onAddToFavorites = { movieToAdd ->
                                coroutineScope.launch {
                                    val wasNewlyAdded = withContext(Dispatchers.IO) {
                                        watchLaterRepo.moveToFavorites(movieToAdd, reviewRepo)
                                    }
                                    refresh()
                                    val message = if (wasNewlyAdded) {
                                        "${movieToAdd.title} aggiunto ai preferiti"
                                    } else {
                                        "${movieToAdd.title} già presente nei preferiti"
                                    }
                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onRemove = { movieToRemove ->
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        watchLaterRepo.removeMovie(movieToRemove)
                                    }
                                    refresh()
                                    Toast.makeText(
                                        context,
                                        "${movieToRemove.title} rimosso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchLaterItem(
    movie: WatchLaterMovie,
    omdbApi: ApiOmdb?,
    apiKey: String,
    onMovieClick: (WatchLaterMovie) -> Unit,
    onAddToFavorites: (WatchLaterMovie) -> Unit,
    onRemove: (WatchLaterMovie) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var posterUrl by remember { mutableStateOf<String?>(null) }
    var year by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movie.externalId, omdbApi, apiKey) {
        val imdbId = movie.externalId
        val api = omdbApi
        if (api != null && imdbId.isNotBlank()) {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getMovieDetails(imdbId, apiKey).execute()
                }
                if (response.isSuccessful) {
                    val body = response.body()
                    posterUrl = body?.Poster
                    year = body?.Year
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { onMovieClick(movie) },
                onLongClick = { expanded = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = movie.title,
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
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Aggiungi ai Preferiti") },
                onClick = {
                    expanded = false
                    onAddToFavorites(movie)
                }
            )//rimossa in quanto buggata */
            DropdownMenuItem(
                text = { Text("Rimuovi") },
                onClick = {
                    expanded = false
                    onRemove(movie)
                }
            )
        }
    }
}