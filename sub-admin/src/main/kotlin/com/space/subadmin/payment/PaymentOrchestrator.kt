package com.space.subadmin.payment

import com.space.subadmin.db.Order
import com.space.subadmin.db.OrderStatus
import com.space.subadmin.orders.CreateOrderRequest
import com.space.subadmin.orders.CreatePendingOrder
import com.space.subadmin.orders.OrderDetail
import com.space.subadmin.orders.OrderRepository
import com.space.subadmin.orders.toDetailDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentOrchestrator(
    private val createPendingOrder: CreatePendingOrder,
    private val payByCash: PayByCash,
    private val orderRepository: OrderRepository,
    private val sagaActions: PaymentSagaActions
) {

    private val logger = LoggerFactory.getLogger(PaymentOrchestrator::class.java)

    /**
     * SAGA-STEP-1: Initiates the order process.
     * Creates an order and sets it to a state where it's awaiting payment confirmation.
     */
    @Transactional
    fun initiateOrder(orderRequest: CreateOrderRequest): OrderDetail {
        logger.info("SAGA-STEP-1: Creating order and pausing for payment confirmation.")
        // Create the initial order using the existing service
        val orderConfirmation = createPendingOrder.execute(orderRequest)

        // Fetch the order with all details needed for the DTO
        val order = orderRepository.findOrderDetailById(orderConfirmation.orderId)
            .orElseThrow { IllegalStateException("Order with ID ${orderConfirmation.orderId} not found after creation.") }


        // Update status to indicate we are waiting for the next step
        order.status = OrderStatus.AWAITING_PAYMENT
        val savedOrder = orderRepository.save(order)
        logger.info("SAGA-STEP-1-SUCCESS: Order ${savedOrder.id} created with status AWAITING_PAYMENT.")
        return savedOrder.toDetailDto()
    }

    /**
     * SAGA-STEP-2 & 3: Resumes the SAGA after external payment confirmation.
     * This function is called when the client confirms cash has been received.
     */
    fun resumeSagaAfterCashConfirmation(orderUuid: String, paymentDetails: CashPaymentRequest) {
        val order = orderRepository.findByUuid(UUID.fromString(orderUuid))
            .orElseThrow { IllegalStateException("Order with ID $orderUuid  found.") }

        // Verify the order is in the correct state to resume
        if (order.status != OrderStatus.AWAITING_PAYMENT) {
            logger.warn("SAGA-RESUME-IGNORED: Order $orderUuid is not in AWAITING_PAYMENT state (current: ${order.status}).")
            return
        }

        var paymentUuid: java.util.UUID? = null
        try {
            // Step 2: Process the payment (Local Transaction 2)
            logger.info("SAGA-STEP-2: Processing payment for order ${order.id}.")
            val paymentConfirmation = payByCash.execute(paymentDetails)
            paymentUuid = paymentConfirmation.uuid
            logger.info("SAGA-STEP-2-SUCCESS: Payment ${paymentUuid} processed successfully.")
            // Step 3: Update inventory (Local Transaction 3)
            logger.info("SAGA-STEP-3: Debiting inventory for order ${order.id}.")
            sagaActions.debitInventory(order)
            logger.info("SAGA-STEP-3-SUCCESS: Inventory debited for order ${order.id}.")

            // Step 4: Mark order as processing (Final Step)
            logger.info("SAGA-STEP-4: Marking order ${order.id} as PROCESSING.")
            sagaActions.markOrderAsProcessing(order)
            logger.info("SAGA-STEP-4-SUCCESS: Order ${order.id} is now PROCESSING. SAGA complete.")

        } catch (ex: Exception) {
            logger.error(
                "SAGA-FAILED: An error occurred while resuming order processing for order $orderUuid: ${ex.message}",
                ex
            )
            // Begin compensating transactions
            compensate(order, paymentUuid)
        }
    }

    private fun compensate(order: Order, paymentUuid: java.util.UUID?) {
        if (paymentUuid != null) {
            logger.info("SAGA-COMPENSATE: Refunding payment $paymentUuid.")
            sagaActions.refundPayment(paymentUuid)
        }
        // Mark the order as FAILED
        logger.info("SAGA-COMPENSATE: Marking order ${order.id} as FAILED.")
        sagaActions.failOrder(order)
    }
}