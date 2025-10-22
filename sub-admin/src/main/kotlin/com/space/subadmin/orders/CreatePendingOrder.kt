package com.space.subadmin.orders

import com.space.subadmin.customers.CustomerRepository
import com.space.subadmin.db.Order
import com.space.subadmin.db.OrderItem
import com.space.subadmin.db.OrderStatus
import com.space.subadmin.products.ProductVariantsRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

// --- Data Transfer Objects (DTOs) ---

data class OrderItemRequest(
    val productId: Long,
    val productVariantId: Long,
    val quantity: Int
)

data class CreateOrderRequest(
    val customerId: Long,
    val items: List<OrderItemRequest>
)

data class OrderConfirmation(
    val orderId: Long,
    val customerId: Long?,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val createdAt: Instant
)

// --- Executioner Service ---

@Service
class CreatePendingOrder(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val productVariantsRepository: ProductVariantsRepository
) {

    @Transactional
    fun execute(request: CreateOrderRequest): OrderConfirmation {
        // 1. Fetch customer
        val customer = customerRepository.findById(request.customerId)
            .orElseThrow { EntityNotFoundException("Customer not found with id: ${request.customerId}") }

        // 2. Create the Order entity first, so we can link items to it
        val now = Instant.now()
        val order = Order(
            customer = customer,
            status = OrderStatus.PENDING,
            createdAt = now,
            updatedAt = now
            // totalAmount will be calculated and set later
        )

        // 3. Create OrderItem entities and establish bidirectional links
        val orderItems = request.items.map { itemRequest ->
            val productVariant = productVariantsRepository.findById(itemRequest.productVariantId)
                .orElseThrow { EntityNotFoundException("Product variant not found with id: ${itemRequest.productVariantId}") }

            // TODO: In a real system, you would also check for sufficient stock here.

            val lineTotal = productVariant.price * BigDecimal(itemRequest.quantity)
            OrderItem(
                quantity = itemRequest.quantity,
                pricePerUnit = productVariant.price,
                lineTotal = lineTotal
            ).apply {
                this.order = order // Set the parent order
                this.productVariant = productVariant
            }
        }

        // 4. Add items to the order and calculate total amount
        order.items.addAll(orderItems)
        order.totalAmount = orderItems.sumOf { it.lineTotal }

        // 5. Save the order. Thanks to CascadeType.ALL, order items will be saved too.
        val savedOrder = orderRepository.save(order)

        // 6. Return a confirmation DTO
        return OrderConfirmation(
            orderId = savedOrder.id,
            customerId = savedOrder.customer?.id,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount,
            createdAt = savedOrder.createdAt!!
        )
    }
}
