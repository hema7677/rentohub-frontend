package com.simats.rentohub

data class BookingDetailsResponse(
    val status: String,
    val message: String,
    val data: BookingFullDetails?
)

data class BookingFullDetails(
    val booking_id: Int?,
    val equipment_name: String?,
    val user_name: String?,
    val user_email: String?,
    val location: String?,
    val days: Int?,
    val daily_rate: String?,
    val total_amount: String?,
    val booking_date: String?,
    val status: String?,
    val image: String?,
    val payment_id: String? = "N/A"
)
