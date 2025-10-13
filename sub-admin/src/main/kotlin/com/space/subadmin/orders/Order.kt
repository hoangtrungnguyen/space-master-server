package com.space.subadmin.orders


import com.space.subadmin.customers.Customer
import com.space.subadmin.products.ProductVariant
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Table(name = "orders")
@Entity
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, updatable = false, unique = true)
    @JdbcTypeCode(SqlTypes.BINARY)
    val uuid: UUID = UUID.randomUUID(),

    @Column(name = "order_date", nullable = false)
    val orderDate: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "total_amount", nullable = false)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "shipping_address", nullable = false, columnDefinition = "LONGVARCHAR")
    val shippingAddress: String = "",

    @Column(name = "billing_address", columnDefinition = "LONGVARCHAR")
    val billingAddress: String? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: List<OrderItem> = listOf(),

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    val createdAt: Instant? = null,

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    var updatedAt: Instant? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    lateinit var customer: Customer
}

@Table(name = "order_items")
@Entity
class OrderItem(
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val quantity: Int = 0,

    @Column(name = "price_per_unit", nullable = false)
    val pricePerUnit: BigDecimal = BigDecimal.ZERO,

    @Column(name = "line_total", nullable = false)
    val lineTotal: BigDecimal = BigDecimal.ZERO
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    lateinit var order: Order

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    lateinit var productVariant: ProductVariant
}

enum class OrderStatus {
    PENDING,
    CANCELED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    FAILED
}
