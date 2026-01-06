package com.simats.rentohub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentAdapter(private val payments: List<BookingItemRemote>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.tvUserName)
        val amount: TextView = view.findViewById(R.id.tvAmount)
        val paymentId: TextView = view.findViewById(R.id.tvPaymentId)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val date: TextView = view.findViewById(R.id.tvDate)
        val location: TextView = view.findViewById(R.id.tvLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.userName.text = payment.user_name ?: "Guest User"
        holder.amount.text = "₹${payment.total_amount}"
        
        // Show Payment ID if it exists, otherwise fallback to Booking ID
        holder.paymentId.text = if (!payment.payment_id.isNullOrEmpty() && payment.payment_id != "N/A") 
                                    "Txn ID: #${payment.payment_id}" 
                                else "Booking ID: #${payment.booking_id}"
        
        val displayStatus = when(payment.status) {
            "Completed" -> "Paid" 
            "Failed" -> "Failed / Cancelled"
            else -> payment.status
        }
        holder.status.text = "Status: $displayStatus"
        
        // Color coding
        when(payment.status) {
            "Completed" -> holder.status.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            "Failed" -> holder.status.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            else -> holder.status.setTextColor(android.graphics.Color.parseColor("#777777"))
        }

        holder.date.text = "Date: ${payment.booking_date}"
        holder.location.text = "Location: ${payment.location ?: "N/A"}"
    }

    override fun getItemCount() = payments.size
}
