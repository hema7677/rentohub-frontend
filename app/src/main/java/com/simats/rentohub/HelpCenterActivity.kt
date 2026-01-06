package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HelpCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnChat = findViewById<Button>(R.id.btnContactAI)

        btnBack.setOnClickListener { finish() }

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}
