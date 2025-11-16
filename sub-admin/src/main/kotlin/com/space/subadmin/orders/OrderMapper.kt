package com.space.subadmin.orders

import com.space.subadmin.common.extensions.toFormattedString
import com.space.subadmin.db.Order
import com.space.subadmin.db.OrderItem
import java.time.format.DateTimeFormatter

fun Order.toDetailDto(): OrderDetail {
    return OrderDetail(
        uuid = this.uuid.toString(),
        orderDate = this.orderDate.toFormattedString("yyyy-MM-dd HH:mm:ss"),
        status = this.status.name,
        totalAmount = this.totalAmount,
        customerName = this.customer?.fullName ?: "N/A",
        shippingAddress = this.shippingAddress,
        billingAddress = this.billingAddress ?: this.shippingAddress,
        items = this.items.map { it.toDetailDto() }
    )
}

fun OrderItem.toDetailDto(): OrderItemDetail {
    return OrderItemDetail(
        productVariantId = this.productVariant.id,
        productName = this.productVariant.product.name,
        quantity = this.quantity,
        pricePerUnit = this.pricePerUnit
    )
}

fun Order.toListItemDto(): OrderListItemDto {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(java.time.ZoneId.systemDefault())
    return OrderListItemDto(
        uuid = this.uuid.toString(),
        orderDate = formatter.format(this.orderDate),
        status = this.status.name,
        totalAmount = this.totalAmount,
        customerName = this.customer?.fullName ?: "N/A"
    )
}
