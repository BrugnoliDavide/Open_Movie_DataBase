/*package com.example.openvideodatabase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SearchMenuActivity : AppCompatActivity() {
    private lateinit var omdbApi: ApiOmdb
    private val apiKey = "ad71433d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_menu_activity)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        omdbApi = retrofit.create(ApiOmdb::class.java)

        val searchButton = findViewById<Button>(R.id.searchButton)
        val titleInput = findViewById<EditText>(R.id.titleEditText)

        searchButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isNotBlank()) {
                searchMovie(title)
            } else {
                Toast.makeText(this, "Inserisci un titolo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchMovie(title: String) {
        val call = omdbApi.searchByTitle(title, apiKey)
        call.enqueue(object : Callback<OmdbSearchResponse> {
            override fun onResponse(call: Call<OmdbSearchResponse>, response: Response<OmdbSearchResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    val movieList = result?.Search
                    if (!movieList.isNullOrEmpty()) {
                        val imdbID = movieList[0].imdbID
                        val intent = Intent(this@SearchMenuActivity, ShowDettailActivity::class.java)
                        intent.putExtra("imdbID", imdbID)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SearchMenuActivity, "Nessun risultato trovato", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchMenuActivity, "Errore nella risposta del server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OmdbSearchResponse>, t: Throwable) {
                Toast.makeText(this@SearchMenuActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}*/


