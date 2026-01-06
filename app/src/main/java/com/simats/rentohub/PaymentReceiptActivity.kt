package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PaymentReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_receipt)

        val txnId = intent.getStringExtra("txn_id") ?: "N/A"
        val amount = intent.getStringExtra("amount") ?: "₹0"
        val location = intent.getStringExtra("location") ?: "N/A"
        val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())

        findViewById<TextView>(R.id.tvAmount).text = amount

        // Helper function to set row values
        fun setRow(rowId: Int, label: String, value: String) {
            val row = findViewById<android.view.View>(rowId)
            row.findViewById<TextView>(R.id.label).text = label
            row.findViewById<TextView>(R.id.value).text = value
        }

        setRow(R.id.rowTxnId, "Transaction ID", txnId)
        setRow(R.id.rowDate, "Date & Time", date)
        setRow(R.id.rowStatus, "Payment Status", "Success")
        setRow(R.id.rowAddress, "Booking Location", location)

        findViewById<android.view.View>(R.id.btnViewBookings).setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            intent.putExtra("NAVIGATE_TO", "BOOKINGS")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, UserMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
