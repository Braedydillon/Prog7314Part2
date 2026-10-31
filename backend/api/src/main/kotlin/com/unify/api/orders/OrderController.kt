package com.unify.api.orders

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/orders")
class OrderController(val orderService: OrderService, val orderRepository: OrderRepository) {

    private val log = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping("/{orderId}")
    fun getOrderById(authentication: Authentication, @PathVariable orderId: Int): ResponseEntity<OrderResponse> {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Order $orderId not found") }

        if (order.userId != authentication.name) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order $orderId not found")
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
        val saved = orderService.updateStatus(authentication.name, id, request.status)
        return ResponseEntity.ok(saved.toResponse())
    }
}
