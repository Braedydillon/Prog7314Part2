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
        val order = orderRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Order $id not found") }

        if (order.userId != authentication.name) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order $id not found")
        }

        val validStatuses = setOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")
        if (request.status !in validStatuses) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status, ${request.status}")
        }

        val previousStatus = order.orderStatus
        order.orderStatus = request.status
        order.updatedAt = LocalDateTime.now()

        val saved = orderRepository.save(order)
        log.info("Order status changed from $previousStatus to ${request.status}")
        return ResponseEntity.ok(saved.toResponse())
    }
}
