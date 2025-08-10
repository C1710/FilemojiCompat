package de.c1710.filemojicompat_testapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import de.c1710.filemojicompat_ui.views.picker.EmojiPackItemAdapter

class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        val emojiPicker: RecyclerView = findViewById(R.id.emoji_preference)
        emojiPicker.adapter = EmojiPackItemAdapter.get(this)

        val backButton: Button = findViewById(R.id.back_to_main)
        backButton.setOnClickListener {
            navigateUpTo(Intent(this, MainActivity::class.java))
        }
    }
}