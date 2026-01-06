package com.simats.rentohub

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val btnApply = findViewById<Button>(R.id.btnApplyFilter)
        val spMin = findViewById<Spinner>(R.id.spMinPrice)
        val spMax = findViewById<Spinner>(R.id.spMaxPrice)

        btnClose.setOnClickListener { finish() }

        // Setup Spinners with fake data
        val minPrices = arrayOf("Min", "₹500", "₹1000", "₹2000", "₹5000")
        val maxPrices = arrayOf("Max", "₹2000", "₹5000", "₹10000", "₹20000+")

        spMin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, minPrices)
        spMax.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, maxPrices)

        btnApply.setOnClickListener {
            Toast.makeText(this, "Filters applied successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
