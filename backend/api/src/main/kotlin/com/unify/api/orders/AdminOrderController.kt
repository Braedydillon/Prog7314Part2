
package com.unify.api.orders

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val orderRepository: OrderRepository
) {

    @GetMapping
    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll()
            .map { it.toResponse() }
    }
}
