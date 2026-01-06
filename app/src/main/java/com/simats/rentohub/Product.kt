package com.simats.rentohub

import android.R

data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val category: String,
    val price_per_day: String,
    val deposit: String,
    val description: String,
    val image: String?,
    val status: String
)

