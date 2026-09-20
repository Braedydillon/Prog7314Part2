package com.unify.api.orders

import com.unify.api.orderitem.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Orders, Int>{
    fun findByUserId(userId: String) : List<Orders>
}