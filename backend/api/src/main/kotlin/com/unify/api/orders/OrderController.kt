package com.unify.api.orders

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/orders")
class OrderController(val orderService: OrderService, val orderRepository: OrderRepository) {

    @GetMapping("/{orderId}")
    fun getOrderById(authentication: Authentication, @PathVariable orderId: Int): ResponseEntity<OrderResponse> {
        val order = orderRepository.findById(orderId).orElse(null)
        ?: return ResponseEntity.notFound().build()

        if (order.userId != authentication.name) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(order.toResponse())
    }

    @GetMapping
    fun getMyOrders(authentication: Authentication): List<OrderResponse> {
        return orderRepository.findByUserId(authentication.name).map { it.toResponse() }
    }

    @PostMapping
    fun createOrder(authentication: Authentication, @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.placeOrder(authentication.name, request)
        return ResponseEntity.ok(order.toResponse())
    }

    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        authentication: Authentication,
        @PathVariable id: Int,
        @RequestBody request: UpdateOrderStatus
    ): ResponseEntity<OrderResponse> {
        val order = orderRepository.findById(id).orElse(null)
        ?: return ResponseEntity.notFound().build()

        if (order.userId != authentication.name) {
            return ResponseEntity.notFound().build()
        }

        val validStatuses = setOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")
        if (request.status !in validStatuses) {
            return ResponseEntity.badRequest().build()
        }

        order.orderStatus = request.status
        order.updatedAt = LocalDateTime.now()

        val saved = orderRepository.save(order)
        return ResponseEntity.ok(saved.toResponse())
    }
}
