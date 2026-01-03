package com.simats.rentohub

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val usertype: String
)
