package com.unify.api.address

data class AddressResponse (
    val id: Int,
    val addressLine: String,
    val city: String,
    val province: String,
    val postalCode: String

)

fun Address.toResponse(): AddressResponse = AddressResponse(
    id = this.id!!,
    addressLine = this.addressLine ?: "",
    city = this.city ?: "",
    province = this.province ?: "",
    postalCode = this.postalCode ?: ""

)
