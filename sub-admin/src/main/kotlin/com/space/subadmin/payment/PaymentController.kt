package com.space.subadmin.payment

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class PaymentController(
    private val payByCash: PayByCash
) {

    /**
     * API endpoint to process a payment made with cash.
     *
     * This endpoint accepts the details of a cash transaction and uses the PayByCash
     * service to record it in the unified payments ledger.
     *
     * @param request The request body containing the order ID, amount, and optional notes.
     * @return A ResponseEntity containing the standardized payment confirmation.
     */
    @PostMapping("/payments/cash")
    fun processCashPayment(@RequestBody request: CashPaymentRequest): ResponseEntity<PaymentConfirmation> {
        // Delegate the entire business logic to the PayByCash executioner class.
        val confirmation = payByCash.execute(request)

        // Return a 200 OK response with the confirmation details.
        return ResponseEntity.ok(confirmation)
    }
}
