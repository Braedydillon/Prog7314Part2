package com.unify.api.address

data class CreateAddressRequest(
    val addressLine: String,
    val city: String,
    val province: String,
    val postalCode: String
)
