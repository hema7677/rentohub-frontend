package com.simats.rentohub

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_page)

        val type = intent.getStringExtra("type") ?: "about"
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = findViewById<TextView>(R.id.tvAboutTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDescription)

        btnBack.setOnClickListener { finish() }

        if (type == "privacy") {
            tvTitle.text = "Privacy Policy"
            tvSubtitle.text = "Protecting Your Rental Data"
            tvDesc.text = "At Rentohub, we take your privacy seriously. Here is how we handle your equipment rental data:\n\n" +
                    "1. Equipment Safety: We record details of the equipment you rent to ensure safety and quality standards.\n\n" +
                    "2. Location Data: We may use your location to find the nearest pickup points for your cameras and lenses.\n\n" +
                    "3. Identity Verification: For high-value items like Sony A7III or DJI Drones, we securely verify your ID to prevent theft.\n\n" +
                    "4. Usage Logs: We maintain logs of rental duration and return times to calculate accurate billing.\n\n" +
                    "5. Third-party Insurance: We share minimal data with our insurance partners to protect you against accidental damage during use.\n\n" +
                    "6. Updates: This policy may be updated as we add more professional filmmaking gear to our catalog."
        } else {
            // Default is About Us
            tvTitle.text = "About Us"
            tvSubtitle.text = "Your Partner in Cinematography"
            tvDesc.text = "Welcome to Rentohub, India's leading platform for professional camera and equipment rentals.\n\n" +
                    "Why Rent from Us?\n" +
                    "• Premium Gear: From RED Cine cameras to basic tripods, we have it all.\n" +
                    "• Quality Checked: Every lens and body is sanitized and sensor-cleaned before delivery.\n" +
                    "• Affordable Rates: We believe professional filmmaking shouldn't break the bank.\n\n" +
                    "Our Story:\n" +
                    "Founded by a group of passionate photographers in 2024, Rentohub started with just two cameras and a dream. Today, we serve over 10,000 creators across the country, helping them capture memories that last a lifetime.\n\n" +
                    "Join our community and start creating today!"
        }
    }
}
