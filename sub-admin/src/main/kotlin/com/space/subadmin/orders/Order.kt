package com.space.subadmin.orders


import com.space.subadmin.customers.Customer
import com.space.subadmin.products.ProductVariant
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Table(name = "orders")
@Entity
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, updatable = false, unique = true)
    @JdbcTypeCode(SqlTypes.BINARY)
    val uuid: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @Column(name = "order_date", nullable = false)
    val orderDate: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal,

    @Column(name = "shipping_address", nullable = false, columnDefinition = "LONGVARCHAR")
    var shippingAddress: String,

    @Column(name = "billing_address", columnDefinition = "LONGVARCHAR")
    var billingAddress: String?,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<OrderItem> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Table(name = "order_items")
@Entity
class OrderItem(
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    val productVariant: ProductVariant,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "price_per_unit", nullable = false)
    val pricePerUnit: BigDecimal,

    @Column(name = "line_total", nullable = false)
    val lineTotal: BigDecimal
)

enum class OrderStatus {
    PENDING,
    CANCELED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    FAILED
}
