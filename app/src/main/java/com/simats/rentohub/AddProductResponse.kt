package com.simats.rentohub

data class AddProductResponse(
    val status: String,
    val message: String,
    val equipment_id: Int?,
    val data: EquipmentData?
)

data class EquipmentData(
    val name: String,
    val brand: String,
    val category: String,
    val daily_rate: String,
    val deposit: String,
    val description: String,
    val image: String
)
