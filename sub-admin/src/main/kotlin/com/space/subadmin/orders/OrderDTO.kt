package com.space.subadmin.orders

import java.math.BigDecimal
import java.time.Instant

data class OrderDetail(
    val id: Long,
    val orderDate: Instant,
    val status: String,
    val totalAmount: BigDecimal,
    val customerName: String,
    val shippingAddress: String,
    val billingAddress: String,
    val items: List<OrderItemDetail>
)

data class OrderItemDetail(
    val productVariantId: Long,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: BigDecimal
) {
    val lineTotal: BigDecimal
        get() = pricePerUnit * BigDecimal(quantity)
}
