package com.example.prog7314p2.Models

import java.io.Serializable

data class AddressResponse (
    val id: Int,
    val addressLine: String,
    val city: String,
    val province: String,
    val postalCode: String
) : Serializable

