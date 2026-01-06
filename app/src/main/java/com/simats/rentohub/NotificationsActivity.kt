package com.simats.rentohub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvNotifications)
        rv.layoutManager = LinearLayoutManager(this)

        val notifications = listOf(
            Notification("Booking Confirmed ✅", "Your Sony A7 III rental is confirmed for tomorrow.", "2m ago", false),
            Notification("Special Offer 🎁", "Get 20% off on Tripods this weekend!", "1h ago", true),
            Notification("Payment Success 💳", "Payment for Booking #7712 was successful.", "3h ago", true),
            Notification("Welcome to Rentohub 🚀", "Start exploring professional cameras for rent.", "1d ago", true)
        )

        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = notifications[position]
                holder.itemView.findViewById<TextView>(R.id.tvNotifTitle).text = item.title
                holder.itemView.findViewById<TextView>(R.id.tvNotifMessage).text = item.message
                holder.itemView.findViewById<TextView>(R.id.tvNotifTime).text = item.time
                holder.itemView.findViewById<View>(R.id.dot).visibility = if (item.isRead) View.GONE else View.VISIBLE
            }

            override fun getItemCount() = notifications.size
        }
    }
}
