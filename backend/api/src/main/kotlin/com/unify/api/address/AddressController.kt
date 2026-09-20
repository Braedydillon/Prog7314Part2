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

    @PutMapping("/{id}")
    fun updateAddress(
        authentication: Authentication,
        @PathVariable("id") id: Int,
        @RequestBody request: CreateAddressRequest
    ) : ResponseEntity<AddressResponse> {
        val address = addressRepository.findById(id).orElse(null)
        ?: return ResponseEntity.notFound().build()

        if(address.userId != authentication.name) {
            return ResponseEntity.notFound().build()
        }

        address.addressLine = request.addressLine
        address.city = request.city
        address.province = request.province
        address.postalCode = request.postalCode

        val saved = addressRepository.save(address)
        return ResponseEntity.ok(saved.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteAddress(
        authentication: Authentication,
        @PathVariable("id") id: Int
    ): ResponseEntity<Void> {
        val address = addressRepository.findById(id).orElse(null)
        ?: return ResponseEntity.notFound().build()

        if (address.userId != authentication.name) {
            return ResponseEntity.notFound().build()
        }
        addressRepository.delete(address)
        return ResponseEntity.noContent().build()
    }
}