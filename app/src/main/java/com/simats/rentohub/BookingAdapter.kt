package com.simats.rentohub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private val bookings: List<BookingItem>,
    private val isAdmin: Boolean = false,
    private val onStatusUpdate: ((BookingItem, String) -> Unit)? = null
) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvBookingName)
        val date: TextView = view.findViewById(R.id.tvBookingDate)
        val price: TextView = view.findViewById(R.id.tvBookingPrice)
        val status: TextView = view.findViewById(R.id.tvBookingStatus)
        val image: ImageView = view.findViewById(R.id.ivBookingImage)
        val btnEditStatus: TextView = view.findViewById(R.id.btnEditStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.name.text = booking.name
        holder.date.text = "Date: ${booking.date}"
        holder.price.text = booking.price
        holder.status.text = booking.status
        
        // Handle images (support both resource IDs and URL/Path strings)
        if (booking.imageRes != 0) {
            holder.image.setImageResource(booking.imageRes)
        } else if (!booking.imageUrl.isNullOrEmpty()) {
            val imageUrl = if (booking.imageUrl.startsWith("http")) {
                booking.imageUrl
            } else {
                RetrofitClient.BASE_URL + booking.imageUrl
            }

            val glideUrl = GlideUrl(
                imageUrl, LazyHeaders.Builder()
                    .addHeader("X-Tunnel-Skip-Anti-Phishing-Page", "true")
                    .build()
            )

            Glide.with(holder.itemView.context)
                .load(glideUrl)
                .placeholder(R.drawable.camera)
                .error(R.drawable.camera)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.camera)
        }

        // Set status color/bg
        updateStatusUI(holder.status, booking.status)

        // Admin Edit logic
        if (isAdmin) {
            holder.btnEditStatus.visibility = View.VISIBLE
            holder.btnEditStatus.setOnClickListener { showStatusDialog(holder.itemView.context, booking) }
        } else {
            holder.btnEditStatus.visibility = View.GONE
        }

        // Show details on click
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Booking Details")
            
            val message = "Item: ${booking.name}\n" +
                          "Date: ${booking.date}\n" +
                          "Current Status: ${booking.status}\n" +
                          "Address: ${if (booking.address.isNotEmpty()) booking.address else "No address provided"}"
            
            builder.setMessage(message)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            if (isAdmin) {
                builder.setNeutralButton("Change Status") { _, _ -> showStatusDialog(context, booking) }
            }
            builder.show()
        }
    }

    private fun showStatusDialog(context: android.content.Context, booking: BookingItem) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Update Status")
        builder.setMessage("Choose action for ${booking.name}:")
        
        builder.setNeutralButton("Pending") { _, _ -> onStatusUpdate?.invoke(booking, "Pending") }
        builder.setPositiveButton("Confirm") { _, _ -> onStatusUpdate?.invoke(booking, "Confirmed") }
        builder.setNegativeButton("Reject") { _, _ -> onStatusUpdate?.invoke(booking, "Cancelled") }
        
        builder.show()
    }

    private fun updateStatusUI(tvStatus: TextView, status: String) {
        tvStatus.text = status
        when (status) {
            "Confirmed", "Coming" -> {
                tvStatus.setTextColor(android.graphics.Color.WHITE)
                tvStatus.setBackgroundResource(R.drawable.bg_status_dark) // Greenish if mapped
            }
            "Pending" -> {
                tvStatus.setTextColor(android.graphics.Color.WHITE)
                // Use a different bg for pending
            }
            "Cancelled" -> {
                tvStatus.setTextColor(android.graphics.Color.WHITE)
            }
        }
        // Fallback for simple coloring if bg_status is used
        try {
            when (status) {
                "Confirmed" -> tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                "Pending" -> tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                "Delivered" -> tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3"))
                "Cancelled" -> tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
            }
        } catch (e: Exception) {}
    }

    override fun getItemCount() = bookings.size
}
