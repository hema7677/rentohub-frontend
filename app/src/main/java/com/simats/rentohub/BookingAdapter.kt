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
            
            try {
                // Prevent showing dialog if activity is finishing
                val activity = context as? android.app.Activity
                if (activity?.isFinishing == true || activity?.isDestroyed == true) return@setOnClickListener

                // Show a "Loading..." dialog first
                val loadingDialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                    .setTitle("Fetching Details")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .show()

                RetrofitClient.api.getBookingDetails(booking.id)
                    .enqueue(object : retrofit2.Callback<BookingDetailsResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<BookingDetailsResponse>,
                            response: retrofit2.Response<BookingDetailsResponse>
                        ) {
                            if (activity?.isFinishing == true || activity?.isDestroyed == true) return
                            loadingDialog.dismiss()
                            
                            if (response.isSuccessful && response.body()?.status == "success") {
                                val details = response.body()?.data
                                if (details != null) {
                                    val message = "--- PRODUCT ---\n" +
                                                  "Item: ${details.equipment_name ?: "N/A"}\n" +
                                                  "Price: ₹${details.daily_rate ?: "0"} / day\n" +
                                                  "Days: ${details.days ?: "0"}\n\n" +
                                                  "--- CUSTOMER ---\n" +
                                                  "Name: ${details.user_name ?: "N/A"}\n" +
                                                  "Email: ${details.user_email ?: "N/A"}\n" +
                                                  "Location: ${details.location ?: "N/A"}\n\n" +
                                                  "--- PAYMENT ---\n" +
                                                  "Total: ₹${details.total_amount ?: "0"}\n" +
                                                  "Status: ${details.status ?: "N/A"}\n" +
                                                  "Date: ${details.booking_date ?: "N/A"}"

                                    com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                                        .setTitle("Full Booking Details")
                                        .setMessage(message)
                                        .setPositiveButton("OK") { d, _ -> d.dismiss() }
                                        .show()
                                } else {
                                    showBasicDetails(context, booking)
                                }
                            } else {
                                showBasicDetails(context, booking)
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<BookingDetailsResponse>, t: Throwable) {
                            if (activity?.isFinishing == true || activity?.isDestroyed == true) return
                            loadingDialog.dismiss()
                            showBasicDetails(context, booking)
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
                showBasicDetails(context, booking)
            }
        }
    }

    private fun showBasicDetails(context: android.content.Context, booking: BookingItem) {
        try {
            val activity = context as? android.app.Activity
            if (activity?.isFinishing == true || activity?.isDestroyed == true) return

            com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("Booking Info")
                .setMessage("Item: ${booking.name}\n" +
                          "Date: ${booking.date}\n" +
                          "Status: ${booking.status}\n" +
                          "Location: ${if (booking.location.isNotEmpty()) booking.location else "N/A"}")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showStatusDialog(context: android.content.Context, booking: BookingItem) {
        try {
            val activity = context as? android.app.Activity
            if (activity?.isFinishing == true || activity?.isDestroyed == true) return

            com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("Update Status")
                .setMessage("Action for ${booking.name}:")
                .setNeutralButton("Pending") { _, _ -> onStatusUpdate?.invoke(booking, "Pending") }
                .setPositiveButton("Confirm") { _, _ -> onStatusUpdate?.invoke(booking, "Confirmed") }
                .setNegativeButton("Reject") { _, _ -> onStatusUpdate?.invoke(booking, "Cancelled") }
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
