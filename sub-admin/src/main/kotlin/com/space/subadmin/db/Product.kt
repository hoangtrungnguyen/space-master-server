package com.space.subadmin.db

import com.space.subadmin.db.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

// =================================================================
//  Supporting Tables ( Categories, Warehouses)
// =================================================================


@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    val parentCategory: Category? = null,

    @OneToMany(mappedBy = "parentCategory")
    val subCategories: Set<Category> = emptySet(),

    @Column(name = "created_at", nullable = false, updatable = false, )
    val createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at", nullable = false, )
    var updatedAt: OffsetDateTime? = null
)

@Entity
@Table(name = "warehouses")
class Warehouse(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String = "",

    val address: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false, )
    val createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at", nullable = false, )
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
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    var category: Category? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variants: MutableSet<ProductVariant> = mutableSetOf(),

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at", nullable = false,)
    @UpdateTimestamp
    var updatedAt: OffsetDateTime? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    lateinit var createdBy: User

    fun updateDetails(
        name: String,
        description: String?,
        category: Category?
    ) {
        this.name = name
        this.description = description
        this.category = category
    }
}

@Entity
@Table(name = "product_variants")
class ProductVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, length = 100, unique = true)
    var sku: String = "",

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    var costPrice: BigDecimal? = null,

    @Column(nullable = false, precision = 8, scale = 2)
    var weight: BigDecimal? = null,

    @Column()
    val attributes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

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
