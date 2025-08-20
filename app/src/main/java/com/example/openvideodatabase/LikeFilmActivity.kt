package com.example.openvideodatabase

// Android
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable

// AndroidX Activity & Compose
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent

// Compose UI
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Animazioni/Transizioni
import androidx.core.app.ActivityOptionsCompat

// Coil per immagini
import coil.compose.AsyncImage

// Tema app
import com.example.openvideodatabase.ui.theme.OpenVideoDatabaseTheme

// Repository e DB locale
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.local.AppDatabase
import com.example.openvideodatabase.data.local.Review

// Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Retrofit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Date e formato
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
                intent.putExtra("from_other_activity", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })

        setupRetrofit()

        val db = AppDatabase.getInstance(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        setContent {
            OpenVideoDatabaseTheme {
                FavoritesScreen(
                    reviewRepository = reviewRepository,
                    omdbApi = omdbApi,
                    apiKey = apiKey,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    reviewRepository: ReviewRepository,
    omdbApi: ApiOmdb?,
    apiKey: String,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var allFavorites by remember { mutableStateOf(emptyList<Review>()) }
    var isLoading by remember { mutableStateOf(true) }
    var sortType by rememberSaveable { mutableStateOf("Alfabetico") }
    var sortAsc by rememberSaveable { mutableStateOf(true) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val favorites = withContext(Dispatchers.IO) { reviewRepository.getAllReviews() }
        allFavorites = favorites
        isLoading = false
    }

    // Lista derivata: filtro + ordinamento, ricalcolata ad ogni battuta
    val filteredFavorites by remember(searchQuery.text, sortType, sortAsc, allFavorites) {
        derivedStateOf {
            val base = if (searchQuery.text.isBlank()) {
                allFavorites
            } else {
                allFavorites.filter {
                    it.title.contains(searchQuery.text, ignoreCase = true) ||
                            it.comment?.contains(searchQuery.text, ignoreCase = true) ?: false
                }
            }
            val sorted = when (sortType) {
                "Rating" -> base.sortedBy { it.rating }
                "Prima visualizzazione" -> base.sortedBy { it.firstViewed?.time ?: 0L }
                else -> base.sortedBy { it.title.lowercase() }
            }
            if (sortAsc) sorted else sorted.asReversed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!searchExpanded) {
                        Text("I tuoi preferiti")
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cerca titolo o commento...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Cerca")
                            },
                            trailingIcon = {
                                if (searchQuery.text.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancella")
                                    }
                                }
                                IconButton(onClick = { searchExpanded = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Chiudi ricerca")
                                }
                            },
                            singleLine = true
                        )
                    }
                },
                navigationIcon = {
                    if (!searchExpanded) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                },
                actions = {
                    if (!searchExpanded) {
                        // Icona di ricerca
                        IconButton(onClick = { searchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Cerca")
                        }

                        // Menu ordinamento
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = "Ordina",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                // Intestazione menu
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Ordina per:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = { /* non cliccabile */ }
                                )

                                listOf("Alfabetico", "Rating", "Prima visualizzazione").forEach { type ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(type)
                                                if (sortType == type) {
                                                    Icon(
                                                        if (sortAsc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                        contentDescription = "Direzione ordinamento",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            // Se già selezionato, cambia solo la direzione
                                            if (sortType == type) {
                                                sortAsc = !sortAsc
                                            } else {
                                                sortType = type
                                                sortAsc = true
                                            }
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
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
                        val intent = Intent(context, WelcomeAndSearchActivity::class.java).apply {
                            putExtra("from_other_activity", true)
                        }
                        (context as? Activity)?.let { act ->
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                act, android.R.anim.fade_in, android.R.anim.fade_out
                            )
                            context.startActivity(intent, options.toBundle())
                        } ?: context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Cerca") },
                    label = { Text("Cerca", fontWeight = FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(Icons.Default.Favorite, contentDescription = "Preferiti", tint = Color.Red)
                    },
                    label = { Text("Preferiti", fontWeight = FontWeight.ExtraBold) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        val intent = Intent(context, WatchLaterActivity::class.java).apply {
                            putExtra("from_other_activity", true)
                        }
                        (context as? Activity)?.let { act ->
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                act, android.R.anim.fade_in, android.R.anim.fade_out
                            )
                            context.startActivity(intent, options.toBundle())
                        } ?: context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Guarda più tardi") },
                    label = { Text("Guarda più tardi", fontWeight = FontWeight.Normal) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Barra di stato ricerca
            if (searchQuery.text.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${filteredFavorites.size} risultati per \"${searchQuery.text}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { searchQuery = TextFieldValue("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancella ricerca",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = "Nessun risultato",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.text.isNotBlank())
                                "Nessun risultato per '${searchQuery.text}'"
                            else
                                "Nessun film nei preferiti",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
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
                                    withContext(Dispatchers.Main) { allFavorites = updated }
                                }
                            },
                            onRate = { reviewToRate: Review, newRating: Float ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    reviewRepository.update(reviewToRate.copy(rating = newRating))
                                    val updated = reviewRepository.getAllReviews()
                                    withContext(Dispatchers.Main) { allFavorites = updated }
                                }
                            },
                            onSetFirstViewed = { reviewToUpdate: Review, newDate: Date ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    reviewRepository.update(reviewToUpdate.copy(firstViewed = newDate))
                                    val updated = reviewRepository.getAllReviews()
                                    withContext(Dispatchers.Main) { allFavorites = updated }
                                }
                            },
                            omdbApi = omdbApi,
                            apiKey = apiKey
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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

    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingInput by remember { mutableStateOf("") }

    var showDateDialog by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf("") }
    val dateFormat = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply { isLenient = false }
    }
    val context = LocalContext.current

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    review.externalId?.let { onShowDetails(it) }
                },
                onLongClick = { expanded = true }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (review.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Valutazione",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", review.rating)}/10",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (review.firstViewed != null) {
                        Text(
                            text = "Visto: ${dateFormat.format(review.firstViewed)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opzioni")
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
                text = { Text("Inserisci valutazione") },
                onClick = {
                    expanded = false
                    showRatingDialog = true
                    ratingInput = if (review.rating > 0) review.rating.toString() else ""
                }
            )
            DropdownMenuItem(
                text = { Text("Imposta prima visualizzazione") },
                onClick = {
                    expanded = false
                    showDateDialog = true
                    dateInput = if (review.firstViewed != null) dateFormat.format(review.firstViewed) else ""
                }
            )
            DropdownMenuItem(
                text = { Text("Elimina", color = Color.Red) },
                onClick = {
                    expanded = false
                    onDelete(review)
                }
            )
        }
    }

    // Dialog valutazione
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Inserisci valutazione") },
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
                    if (rating != null && rating in 0f..10f) {
                        onRate(review, rating)
                        showRatingDialog = false
                        ratingInput = ""
                    } else {
                        Toast.makeText(context, "Inserisci un valore tra 0 e 10", Toast.LENGTH_SHORT).show()
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

    // Dialog data
    if (showDateDialog) {
        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = { Text("Inserisci data prima visualizzazione") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { newValue ->
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