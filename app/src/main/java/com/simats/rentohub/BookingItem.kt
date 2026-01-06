package com.simats.rentohub

data class BookingItem(
    val id: String,
    val name: String,
    val date: String,
    val price: String,
    val status: String, // e.g., "Confirmed", "Pending", "Delivered"
    val imageRes: Int = 0, // 0 means use imageUrl
    val location: String = "",
    val imageUrl: String? = null
)
