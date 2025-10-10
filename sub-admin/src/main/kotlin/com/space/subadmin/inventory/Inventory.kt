package com.space.subadmin.inventory

import com.space.subadmin.products.ProductVariant
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.*

enum class InventoryTransactionType {
    sale, restock, `return`, adjustment
}

// =================================================================
//  Inventory Tables
// =================================================================

@Entity
@Table(
    name = "inventory",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_variant_id"])]
)
class Inventory(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    val productVariant: ProductVariant,

    @Column(name = "quantity_on_hand", nullable = false)
    var quantityOnHand: Int = 0,

    @Column(name = "quantity_committed", nullable = false)
    var quantityCommitted: Int = 0,

    @Column(name = "reorder_level", nullable = false)
    var reorderLevel: Int = 0,

    @Column(name = "last_restocked_at")
    var lastRestockedAt: OffsetDateTime? = null,

    @OneToMany(mappedBy = "inventory", cascade = [CascadeType.ALL], orphanRemoval = true)
    val transactions: MutableList<InventoryTransaction> = mutableListOf()
)

@Entity
@Table(name = "inventory_transactions")
class InventoryTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    val inventory: Inventory,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: InventoryTransactionType,

    @Column(nullable = false)
    val quantity: Int,

    val notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)

