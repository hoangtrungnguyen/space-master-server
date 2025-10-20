package com.space.subadmin.orders


import com.space.subadmin.customers.Customer
import com.space.subadmin.products.ProductVariant
import io.hypersistence.utils.hibernate.id.TsidGenerator
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.Instant

@Table(name = "orders")
@Entity
data class Order(
    @Id
    @GenericGenerator(name = "tsid", strategy = "com.space.subadmin.config.TsidGenerator")
    @GeneratedValue(generator = "tsid")
    val id: Long = 0,

    @Column(name = "order_date", nullable = false)
    val orderDate: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "shipping_address", nullable = false)
    val shippingAddress: String = "",

    @Column(name = "billing_address")
    val billingAddress: String? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<OrderItem> = mutableListOf(), // Use val for immutable collection reference

    @Column(name = "created_at", nullable = false, updatable = false,)
    var createdAt: Instant? = null, // Let @PrePersist manage this

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    var customer: Customer? = null
) {
    @PrePersist
    fun onPrePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }

    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }
}

@Table(name = "order_items")
@Entity
data class OrderItem(
    @Id
    @GenericGenerator(name = "tsid", strategy = "com.space.subadmin.config.TsidGenerator")
    @GeneratedValue(generator = "tsid")
    val id: Long = 0,

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
