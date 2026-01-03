package com.simats.rentohub

data class EquipmentResponse(
    val status: String,
    val count: Int,
    val data: List<Equipment>
)
