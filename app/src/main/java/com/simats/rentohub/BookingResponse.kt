package com.simats.rentohub

data class BookingResponse(
    val status: String,
    val message: String,
    val booking_id: Int?,
    val data: BookingData?
)

data class BookingData(
    val equipment_id: String,
    val equipment_name: String,
    val image: String,
    val daily_rate: String,
    val days: String,
    val total_amount: Double
)
