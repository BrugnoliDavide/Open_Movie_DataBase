package com.example.openvideodatabase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.Icons
import androidx.compose.material3.*

import androidx.activity.OnBackPressedCallback

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//barra di navigazione
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.app.Activity
import androidx.core.app.ActivityOptionsCompat
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme


class LikeFilmActivity : ComponentActivity() {

    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LikeFilmActivity, WelcomeAndSearchActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })




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
                    title = { Text("Film Preferiti") },

                    //il tasto indietro è stato deprecato dal momento che inserendo la NavBar questo è privo di utilità
                    /*navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Torna indietro")
                    }
                }*/
                )
            }, bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val intent = Intent(context, WelcomeAndSearchActivity::class.java)

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
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cerca",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        label = { Text("Cerca", fontWeight = FontWeight.Normal) }
                    )

                    NavigationBarItem(
                        selected = true, // Mostra come selezionato
                        onClick = { /* Nessuna azione */ },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Preferiti",
                                tint = Color.Red
                            )
                        },
                        label = { Text("Preferiti", fontWeight = FontWeight.Bold) }
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
                            allFavorites.filter {
                                it.title.contains(query.text, ignoreCase = true)
                            }
                        }
                    },
                    label = { Text("Cerca...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))


                LazyColumn {
                    items(filteredFavorites) { review ->
                        FavoriteItem(
                            review = review,
                            onShowDetails = { imdbId ->
                                val intent =
                                    Intent(context, ShowDettailActivity::class.java).apply {
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
                                            it.title.contains(
                                                searchQuery.text,
                                                ignoreCase = true
                                            )
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
                                                it.title.contains(
                                                    searchQuery.text,
                                                    ignoreCase = true
                                                )
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
                                                it.title.contains(
                                                    searchQuery.text,
                                                    ignoreCase = true
                                                )
                                            }
                                    }

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
    fun FavoriteItem(
        review: Review,
        onShowDetails: (String) -> Unit,
        onDelete: (Review) -> Unit,
        onRate: (Review, Float) -> Unit,
        onSetFirstViewed: (Review, Date) -> Unit
    ) {
        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }

        // Stato per mostrare dialogo di inserimento valutazione
        var showRatingDialog by remember { mutableStateOf(false) }
        var ratingInput by remember { mutableStateOf("") }

        // Stato per mostrare dialogo di inserimento data
        var showDateDialog by remember { mutableStateOf(false) }
        var dateInput by remember { mutableStateOf("") }

        val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .combinedClickable(
                        onClick = { onShowDetails(review.externalId) },
                        onLongClick = { expanded = true }
                    ),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = review.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                        review.externalId?.let { imdbId -> onShowDetails(imdbId) }
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

        if (showRatingDialog) {
            AlertDialog(
                onDismissRequest = { showRatingDialog = false },
                title = {
                    Text(
                        text = "Inserisci valutazione",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },

                text = {
                    OutlinedTextField(
                        value = ratingInput,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                val value = newValue.toFloatOrNull()
                                if (value == null || (value in 0f..10f)) {
                                    ratingInput = newValue
                                }
                            }

                        },

                        label = { Text("Valutazione (0-10)") },
                        /*colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),*/
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
                        }
                    }) {
                        Text("Conferma")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRatingDialog = false
                        ratingInput = ""
                    }) {
                        Text("Annulla")
                    }
                }
            )
        }

        if (showDateDialog) {
            AlertDialog(
                onDismissRequest = { showDateDialog = false },
                title = { Text(text = "Inserisci data prima visualizzazione") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { newValue ->
                                if (newValue.matches(Regex("^\\d{0,4}-?\\d{0,2}-?\\d{0,2}\$"))) {
                                    dateInput = newValue
                                }
                            },
                            label = { Text("Data (dd-mm-yyyy)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {

                            dateInput = dateFormat.format(Date())
                        }) {
                            Text("Imposta data di oggi")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            val date = dateFormat.parse(dateInput)
                            if (date != null) {
                                onSetFirstViewed(review, date)
                                showDateDialog = false
                                dateInput = ""
                            }
                        } catch (e: Exception) {
                        }
                    }) {
                        Text("Conferma")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDateDialog = false
                        dateInput = ""
                    }) {
                        Text("Annulla")
                    }
                }
            )
        }
    }
}



