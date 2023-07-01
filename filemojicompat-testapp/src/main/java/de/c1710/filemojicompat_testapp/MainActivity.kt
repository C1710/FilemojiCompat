package de.c1710.filemojicompat_testapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val prefButton: Button = findViewById(R.id.preference_button)
        prefButton.setOnClickListener {
            startActivity(Intent(this, PreferenceActivity::class.java))
        }
    }
}