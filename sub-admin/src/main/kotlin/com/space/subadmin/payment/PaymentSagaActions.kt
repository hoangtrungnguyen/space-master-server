package com.space.subadmin.payment

import com.space.subadmin.db.Order
import com.space.subadmin.db.OrderStatus
import com.space.subadmin.db.PaymentStatus
import com.space.subadmin.inventory.InventoryService
import com.space.subadmin.orders.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class PaymentSagaActions(
    private val inventoryService: InventoryService,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) {
    @Transactional(propagation = Propagation.REQUIRED)
    fun debitInventory(order: Order) {
        order.items.forEach { item ->
            inventoryService.subtractQuantity(item.productVariant.id!!, item.quantity)
        }
    }

    @Transactional
    fun markOrderAsProcessing(order: Order) {
        val orderToUpdate = orderRepository.findById(order.id).get()
        orderToUpdate.status = OrderStatus.PROCESSING
        orderRepository.save(orderToUpdate)
    }

    @Transactional
    fun refundPayment(paymentUuid: UUID) {
        val payment = paymentRepository.findOneByUuid(paymentUuid)
        payment!!.status = PaymentStatus.REFUNDED
        paymentRepository.save(payment)
    }

    @Transactional
    fun failOrder(order: Order) {
        val orderToFail = orderRepository.findById(order.id).get()
        orderToFail.status = OrderStatus.FAILED
        orderRepository.save(orderToFail)
    }
}
