package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PaymentSuccessActivity : AppCompatActivity() {

    private var isDetailsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        val txnId = intent.getStringExtra("txn_id") ?: "#TXN${System.currentTimeMillis() / 1000}"
        val amount = intent.getStringExtra("amount") ?: "₹0"
        val address = intent.getStringExtra("address") ?: "No address provided"

        // Transition logic: Show Congrats for 1.5 seconds, then go to Receipt
        Handler(Looper.getMainLooper()).postDelayed({
            val receiptIntent = Intent(this, PaymentReceiptActivity::class.java)
            receiptIntent.putExtras(intent) // Pass txn_id, amount, address
            startActivity(receiptIntent)
            finish()
        }, 1500)

        // Allow skipping the wait by clicking the screen
        findViewById<View>(android.R.id.content).setOnClickListener {
            val receiptIntent = Intent(this, PaymentReceiptActivity::class.java)
            receiptIntent.putExtras(intent)
            startActivity(receiptIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Do nothing during transition
    }
}
