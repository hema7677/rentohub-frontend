package com.simats.rentohub

data class DeleteResponse(
    val status: String,
    val message: String,
    val deleted_id: Int?
)
