package com.space.subadmin.payment

import com.space.subadmin.db.Order
import com.space.subadmin.orders.CreateOrderRequest
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/dev/payments")
@Profile("dev")
class DevPaymentController(
    private val paymentOrchestrator: PaymentOrchestrator
) {

    /**
     * Dev endpoint to initiate a new order.
     * This simulates the first step of the SAGA.
     * @param orderRequest The request body containing customer and item details.
     * @return The created order with AWAITING_PAYMENT status.
     *
     * Example Request Body:
     * ```json
     * {
     *   "customerId": 1,
     *   "items": [
     *     {
     *       "productVariantId": 101,
     *       "quantity": 2
     *     },
     *     {
     *       "productVariantId": 102,
     *       "quantity": 1
     *     }
     *   ]
     * }
     * ```
     */
    @PostMapping("/initiate")
    fun initiateOrder(@RequestBody orderRequest: CreateOrderRequest): Order {
        return paymentOrchestrator.initiateOrder(orderRequest)
    }

    /**
     * Dev endpoint to resume the SAGA with a cash payment confirmation.
     * This simulates the client confirming cash payment.
     * @param orderId The ID of the order to resume.
     * @param request A DTO containing the payment amount.
     *
     * Example Request:
     * POST /dev/payments/{orderId}/resume-cash
     *
     * Example Request Body:
     * ```json
     * {
     *   "amount": 150.75
     * }
     * ```
     */
    @PostMapping("/{orderId}/resume-cash")
    fun resumeSagaAfterCashConfirmation(
        @PathVariable orderId: Long,
        @RequestBody request: CashPaymentRequestDto
    ) {
        // In a real app, you'd get amount from a trusted source. Here we use a DTO for dev purposes.
        val paymentDetails = CashPaymentRequest(orderId = orderId, amount = request.amount)
        paymentOrchestrator.resumeSagaAfterCashConfirmation(orderId, paymentDetails)
    }

    data class CashPaymentRequestDto(val amount: BigDecimal)
}
