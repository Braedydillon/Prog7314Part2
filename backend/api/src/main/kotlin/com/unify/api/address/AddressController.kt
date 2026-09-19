package com.unify.api.address

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/addresses")
class AddressController(private val addressRepository: AddressRepository) {

    @GetMapping
    fun getMyAddresses(authentication: Authentication): List<AddressResponse> {
        val userId = authentication.name
        return addressRepository.findByUserId(userId).map { it.toResponse() }
    }

    @PostMapping
    fun createAddress(
        authentication: Authentication,
        @RequestBody request: CreateAddressRequest
    ): ResponseEntity<AddressResponse> {
        val address = Address().apply {
            userId = authentication.name
            addressLine = request.addressLine
            city = request.city
            province = request.province
            postalCode = request.postalCode
        }
        val saved = addressRepository.save(address)
        return ResponseEntity.ok(saved.toResponse())
    }
}