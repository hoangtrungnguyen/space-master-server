package com.space.subadmin.inventory

import com.space.subadmin.products.ProductVariant
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
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
    val id: UUID = UUID.randomUUID(),

    @Column(name = "quantity_on_hand", nullable = false)
    var quantityOnHand: Int = 0,

    @Column(name = "quantity_committed", nullable = false)
    val quantityCommitted: Int = 0,

    @Column(name = "reorder_level", nullable = false)
    val reorderLevel: Int = 0,

    @Column(name = "last_restocked_at")
    val lastRestockedAt: OffsetDateTime? = null,

    @OneToMany(mappedBy = "inventory", cascade = [CascadeType.ALL], orphanRemoval = true)
    val transactions: MutableList<InventoryTransaction> = mutableListOf()
) {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    lateinit var productVariant: ProductVariant
}

@Entity
@Table(name = "inventory_transactions")
class InventoryTransaction(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: InventoryTransactionType = InventoryTransactionType.adjustment,

    @Column(nullable = false)
    val quantity: Int = 0,

    val notes: String? = null,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    lateinit var createdAt: OffsetDateTime
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    lateinit var inventory: Inventory
}
