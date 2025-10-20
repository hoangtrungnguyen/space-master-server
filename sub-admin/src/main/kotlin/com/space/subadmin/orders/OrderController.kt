package com.space.subadmin.orders

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

    /**
     * Creates a new order with a 'PENDING' status.
     *
     * @param request The request body containing customer and order item details.
     * @return A [ResponseEntity] with the [OrderConfirmation] and HTTP status 201 (Created).
     */
    @PostMapping("/create-pending")
    fun createPendingOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderConfirmation> {
        val orderConfirmation = createPendingOrder.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(orderConfirmation)
    }
}
