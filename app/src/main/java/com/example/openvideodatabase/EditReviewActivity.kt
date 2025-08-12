package com.example.openvideodatabase

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.example.openvideodatabase.data.local.Review
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.openvideodatabase.data.ReviewRepository
import com.example.openvideodatabase.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditReviewActivity : ComponentActivity() {
    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        reviewRepository = ReviewRepository(db.reviewDao())

        val reviewId = intent.getLongExtra("REVIEW_ID", -1L)

        setContent {
            EditReviewScreen(
                reviewRepository = reviewRepository,
                reviewId = reviewId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReviewScreen(
    reviewRepository: ReviewRepository,
    reviewId: Long,
    onBack: () -> Unit
) {
    var review by remember { mutableStateOf<Review?>(null) }
    var rating by remember { mutableStateOf(0f) }
    var firstViewed by remember { mutableStateOf<Date?>(null) }
    var comment by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(reviewId) {
        coroutineScope.launch(Dispatchers.IO) {
            val loadedReview = reviewRepository.getReviewById(reviewId)
            withContext(Dispatchers.Main) {
                review = loadedReview
                loadedReview?.let {
                    rating = it.rating
                    firstViewed = it.firstViewed
                    comment = it.comment ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifica Recensione") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            review?.let {
                                val updatedReview = it.copy(
                                    rating = rating,
                                    firstViewed = firstViewed,
                                    comment = comment
                                )
                                coroutineScope.launch(Dispatchers.IO) {
                                    reviewRepository.update(updatedReview)
                                    withContext(Dispatchers.Main) {
                                        onBack()
                                    }
                                }
                            }
                        },
                        enabled = review != null
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Salva")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            review?.let {
                // Campo Valutazione
                Text("Valutazione: ${"%.1f".format(rating)}")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..10f,
                    steps = 20,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Data
                val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                val calendar = Calendar.getInstance()

                Text("Prima visualizzazione:")
                OutlinedTextField(
                    value = firstViewed?.let { dateFormatter.format(it) } ?: "Non impostata",
                    onValueChange = { /* Non modificabile direttamente */ },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Scegli data")
                        }
                    }
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            firstViewed = calendar.time
                            showDatePicker = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Commento
                Text("Commento:")
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}