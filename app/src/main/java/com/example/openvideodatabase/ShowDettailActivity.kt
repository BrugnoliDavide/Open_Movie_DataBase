//show dettail
package com.example.openvideodatabase

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ShowDettailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showdettail)

        val imdbID = intent.getStringExtra("imdbID")
        val textView = findViewById<TextView>(R.id.imdbIdTextView)
        textView.text = "IMDb ID ricevuto: $imdbID"
    }
}
