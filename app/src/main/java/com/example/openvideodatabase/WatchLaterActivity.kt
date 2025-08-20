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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.WatchLaterRepo
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.WatchLaterMovie
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterActivity : ComponentActivity() {

    private lateinit var watchLaterRepo: WatchLaterRepo
    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gestione back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@WatchLaterActivity, WelcomeAndSearchActivity::class.java)
                intent.putExtra("from_other_activity", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })

        val db = AppDatabase.getInstance(applicationContext)
        watchLaterRepo = WatchLaterRepo(db.watchLaterDao())
        reviewRepository = ReviewRepository(db.reviewDao())

        setContent {
            OpenVideoDatabaseTheme {
                WatchLaterScreen(
                    watchLaterRepo = watchLaterRepo,
                    reviewRepo = reviewRepository,
                    onBack = { finish() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun WatchLaterScreen(
        watchLaterRepo: WatchLaterRepo,
        reviewRepo: ReviewRepository,
        onBack: () -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var allWatchLater by remember { mutableStateOf(listOf<WatchLaterMovie>()) }
        var filteredWatchLater by remember { mutableStateOf(listOf<WatchLaterMovie>()) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Funzione per aggiornare la lista
        val updateMovieList = suspend {
            val updated = withContext(Dispatchers.IO) { watchLaterRepo.getAllMovies() }
            allWatchLater = updated
            filteredWatchLater = if (searchQuery.isEmpty()) updated
            else updated.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

        // Carica film "guarda più tardi"
        LaunchedEffect(Unit) {
            val list = withContext(Dispatchers.IO) { watchLaterRepo.getAllMovies() }
            allWatchLater = list
            filteredWatchLater = list
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Guarda più tardi") }) },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val intent = Intent(context, WelcomeAndSearchActivity::class.java)
                            intent.putExtra("from_other_activity", true) // Evita schermata di benvenuto
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
                        selected = false,
                        onClick = {
                            val intent = Intent(context, LikeFilmActivity::class.java)
                            intent.putExtra("from_other_activity", true) // Evita schermata di benvenuto
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
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Preferiti") },
                        label = { Text("Preferiti", fontWeight = FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* schermo corrente */ },
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
                    onValueChange = { query ->
                        searchQuery = query
                        filteredWatchLater = if (query.isEmpty()) allWatchLater
                        else allWatchLater.filter { it.title.contains(query, ignoreCase = true) }
                    },
                    label = { Text("Cerca...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(items = filteredWatchLater, key = { it.id }) { movie ->
                        WatchLaterItem(
                            movie = movie,
                            onAddToFavorites = { movieToAdd ->
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        watchLaterRepo.moveToFavorites(movieToAdd, reviewRepo)
                                    }
                                    updateMovieList()
                                    Toast.makeText(context, "${movieToAdd.title} aggiunto ai preferiti", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRemove = { movieToRemove ->
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        watchLaterRepo.removeMovie(movieToRemove)
                                    }
                                    updateMovieList()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun WatchLaterItem(
        movie: WatchLaterMovie,
        onAddToFavorites: (WatchLaterMovie) -> Unit,
        onRemove: (WatchLaterMovie) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = { /* mostra dettagli se vuoi */ },
                    onLongClick = { expanded = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Aggiungi ai Preferiti") },
                        onClick = {
                            expanded = false
                            onAddToFavorites(movie)
                        }
                    )
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
    }
}