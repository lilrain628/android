package com.example.mycalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val buttonGoToCalculator = findViewById<Button>(R.id.buttonGoToCalculator)
        val buttonGoTomusicApp = findViewById<Button>(R.id.buttonMusic)
        val buttonGoToGPSApp = findViewById<Button>(R.id.buttonGps)
        val buttonGoToserverApp = findViewById<Button>(R.id.buttonServer)
        val buttonGoToTelephony = findViewById<Button>(R.id.buttonGoToTelephony)


        buttonGoToCalculator.setOnClickListener {
            val intent = Intent(this, calcActivity::class.java)
            startActivity(intent)
        }


        buttonGoTomusicApp.setOnClickListener {
            val intent = Intent(this, MusicActivity::class.java)
            startActivity(intent)
        }

        buttonGoToGPSApp.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        buttonGoToserverApp.setOnClickListener {
            val intent = Intent(this, serverActivity::class.java)
            startActivity(intent)
        }

        buttonGoToTelephony.setOnClickListener {
            val intent = Intent(this, TelephonyActivity::class.java)
            startActivity(intent)
        }
    }
}