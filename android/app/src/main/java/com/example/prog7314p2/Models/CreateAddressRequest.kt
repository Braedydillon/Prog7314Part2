package com.example.prog7314p2.Models

data class CreateAddressRequest(
    val addressLine: String,
    val city: String,
    val province: String,
    val postalCode: String
)