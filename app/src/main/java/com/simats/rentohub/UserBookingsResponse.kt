package com.simats.rentohub

data class UserBookingsResponse(
    val status: String,
    val message: String,
    val data: List<BookingItemRemote>?
)

data class BookingItemRemote(
    val booking_id: Int?,
    val equipment_id: Int?,
    val equipment_name: String?,
    val user_name: String?,
    val image: String?,
    val daily_rate: String?,
    val days: Int?,
    val total_amount: String?,
    val booking_date: String?,
    val status: String?, 
    val location: String?,
    val payment_id: String?
)
