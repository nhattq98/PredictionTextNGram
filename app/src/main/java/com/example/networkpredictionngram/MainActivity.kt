package com.example.networkpredictionngram

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.predictionlib.Predictor
import com.example.predictionlib.listener.OnPredictListener

class MainActivity : AppCompatActivity() {
    private var predictor: Predictor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        predictor = Predictor(this)
        repeat(100) {
            val random = ('a'..'z').random()
            predictor?.predictCurrentWordAsync(3, "th${random}", object : OnPredictListener {
                override fun onPredict(words: List<String?>?) {
                    Log.d("tahn_check", "$words")
                }
            })
        }

    }
}