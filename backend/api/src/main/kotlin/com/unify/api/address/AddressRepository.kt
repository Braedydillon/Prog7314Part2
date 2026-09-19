package com.unify.api.address

import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Int> {
    fun findByUserId(userId: String): List<Address>
}