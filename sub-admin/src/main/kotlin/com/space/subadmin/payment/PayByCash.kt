package com.space.subadmin.payment

import com.space.subadmin.db.CashDetails
import com.space.subadmin.db.Currency
import com.space.subadmin.db.Payment
import com.space.subadmin.db.PaymentMethodType
import com.space.subadmin.db.PaymentStatus
import com.space.subadmin.orders.OrderRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// --- Data Transfer Objects (DTOs) for the API Layer ---

/**
 * The request body for creating a new cash payment.
 * The client only needs to specify which order is being paid for.
 */
data class CashPaymentRequest(
    val orderUuid: UUID, // The internal ID of the Order entity
    val notes: String? = null,
    val amount: BigDecimal
)

/**
 * The confirmation response sent back to the client.
 */
data class PaymentConfirmation(
    val uuid: UUID,
    val orderId: Long, // The public-facing UUID of the order
    val amountPaid: BigDecimal,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethodType,
    val confirmedAt: Instant
)

// --- Executioner Service ---

/**
 * Service class responsible for handling and recording a cash payment
 * by creating a payment record that mirrors an existing order.
 */
@Service
class PayByCash(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository // Inject the repository to find orders
) {

    /**
     * Executes the cash payment process by creating a new Payment record based on an Order.
     *
     * @param request The details of the cash payment from the client.
     * @return A confirmation DTO after the payment is successfully saved.
     * @throws EntityNotFoundException if the orderId does not correspond to an existing order.
     */
    @Transactional
    fun execute(request: CashPaymentRequest): PaymentConfirmation {
        // 1. Fetch the order from the database using its internal ID.
        val order = orderRepository.findByUuid(request.orderUuid)
            .orElseThrow { EntityNotFoundException("Order not found with id: ${request.orderUuid}") }

        // 2. Create the unified Payment entity.
        val payment = Payment(
            metadata = CashDetails(notes = request.notes).toString()
        ).apply {
            this.order = order
            this.amount = order.totalAmount
            this.status = PaymentStatus.COMPLETED
            this.paymentMethod = PaymentMethodType.CASH
            this.currency = Currency.VND
        }

        // 4. Save the single, unified Payment object and its details to the database.
        val savedPayment = paymentRepository.save(payment)

        order.payment = savedPayment
        // 5. Return a standardized confirmation DTO.
        return PaymentConfirmation(
            uuid = savedPayment.uuid,
            orderId = savedPayment.order.id, // Use the public-facing UUID of the order
            amountPaid = savedPayment.amount,
            status = savedPayment.status,
            paymentMethod = savedPayment.paymentMethod,
            confirmedAt = savedPayment.createdAt
        )
    }
}
