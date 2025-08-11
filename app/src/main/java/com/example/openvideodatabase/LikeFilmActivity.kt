package com.example.openvideodatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LikeFilmActivity : ComponentActivity() {

    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzo il repository
        val db = AppDatabase.getInstance(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        setContent {
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

    // Carica tutti i preferiti dal DB quando la schermata si apre
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val favorites = reviewRepository.getAllReviews() // Devi avere questa funzione nel DAO
            withContext(Dispatchers.Main) {
                allFavorites = favorites
                filteredFavorites = favorites
            }
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Film Preferiti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp)) {

            // Barra di ricerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    filteredFavorites = if (query.text.isEmpty()) {
                        allFavorites
                    } else {
                        allFavorites.filter {
                            it.title.contains(query.text, ignoreCase = true)
                        }
                    }
                },
                label = { Text("Cerca...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista dei preferiti filtrata
            LazyColumn {
                items(filteredFavorites) { review ->
                    FavoriteItem(review)
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = review.title, style = MaterialTheme.typography.titleMedium)
            if (!review.comment.isNullOrEmpty()) {
                Text(text = review.comment!!, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
