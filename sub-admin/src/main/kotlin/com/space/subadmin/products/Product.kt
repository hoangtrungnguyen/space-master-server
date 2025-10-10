package com.space.subadmin.products

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

// =================================================================
//  Supporting Tables (Brands, Categories, Warehouses)
// =================================================================

@Entity
@Table(name = "brands")
class Brand(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    val name: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    var id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    val parentCategory: Category? = null,

    @OneToMany(mappedBy = "parentCategory")
    val subCategories: Set<Category> = emptySet(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)

@Entity
@Table(name = "warehouses")
class Warehouse(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    var id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    val address: String?,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)

// =================================================================
//  Core Product Tables (Products, Product Variants)
// =================================================================

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    var description: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    var brand: Brand?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category?,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variants: MutableSet<ProductVariant> = mutableSetOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
) {

    fun updateDetails(
        name: String,
        description: String?,
        brand: Brand?,
        category: Category?
    ) {
        this.name = name
        this.description = description
        this.brand = brand
        this.category = category
    }
}

@Entity
@Table(name = "product_variants")
class ProductVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false, length = 100, unique = true)
    var sku: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    var costPrice: BigDecimal?,

    @Column(nullable = false, precision = 8, scale = 2)
    var weight: BigDecimal?,

    /**
     * For full JSONB support with a Map or data class, you'll need a library like hypersistence-utils.
     * Example with hypersistence-utils:
     *
     *   @Type(JsonType::class)
     *   @Column(columnDefinition = "jsonb")
     *   val attributes: Map<String, Any>? = null,
     */
    @Column(columnDefinition = "LONGVARCHAR")
    val attributes: String? = null, // Storing as String for simplicity, assuming JSON string

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
) {
    fun updateDetails(
        sku: String,
        price: BigDecimal,
        costPrice: BigDecimal?,
        weight: BigDecimal?
    ) {
        this.sku = sku
        this.price = price
        this.costPrice = costPrice
        this.weight = weight
    }
}


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
