package com.space.subadmin.orders

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val createPendingOrder: CreatePendingOrder
) {

    @PostMapping("/create-pending")
    fun createPendingOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderConfirmation> {
        val orderConfirmation = createPendingOrder.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(orderConfirmation)
    }

}
