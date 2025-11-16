package com.space.subadmin.payment

import com.space.subadmin.orders.CreateOrderRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class PaymentController(
    private val paymentOrchestrator: PaymentOrchestrator
) {

    /**
     * Initiates the SAGA by creating an order with status AWAITING_PAYMENT.
     */
    @PostMapping("/orders/initiate")
    fun initiateOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<Map<String, Any>> {
        val order = paymentOrchestrator.initiateOrder(request)
        return ResponseEntity.ok(mapOf("orderUuid" to order.uuid, "status" to order.status))
    }

    /**
     * API endpoint to confirm a cash payment for an existing order and resume the SAGA.
     */
    @PostMapping("/orders/{orderUuid}/confirm-cash-payment")
    fun confirmCashPayment(
        @PathVariable orderUuid: String,
        @RequestBody request: CashPaymentRequest
    ): ResponseEntity<Unit> {
//         Resume the SAGA with the payment details.
        paymentOrchestrator.resumeSagaAfterCashConfirmation(orderUuid, request)
        return ResponseEntity.ok().build()
    }


    @PostMapping("/orders/{orderId}/cancel-payment")
    fun cancelOrder(uuid: String): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok().build()
    }
}
