package com.example.rentalapp   // ← change if your package is different

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.rentohub.R

class PaymentActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val bookingId = intent.getIntExtra("BOOKING_ID", 0)
        val amount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)

        findViewById<TextView>(R.id.txtAmount).text = "Pay ₹$amount"

        findViewById<Button>(R.id.btnPay).setOnClickListener {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
            finish()   // ✅ correct
        }
    }
}
