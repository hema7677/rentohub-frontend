package com.simats.rentohub

data class BookingResponse(
    val status: String,
    val message: String,
    val booking_id: Int?,
    val data: BookingData?
)

data class BookingData(
    val equipment_id: Int,
    val equipment_name: String,
    val image: String,
    val daily_rate: Double,
    val days: Int,
    val total_amount: Double
)
