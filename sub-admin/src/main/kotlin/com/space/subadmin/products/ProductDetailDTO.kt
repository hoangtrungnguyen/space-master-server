package com.space.subadmin.products

import com.space.subadmin.db.InventoryTransactionType
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * A comprehensive DTO for the product detail page.
 */
data class ProductDetailDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val categoryName: String?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime?,
    val createdByUsername: String,
    val variants: List<ProductVariantDetailTransactionsDTO>,
){
    /**
     * Nested DTO for parent product information.
     */
    data class ProductInfo(
        val id: Long,
        val name: String,
        val description: String?,
        val brandName: String?,
        val categoryName: String?,
        val createdAt: OffsetDateTime?,
        val createdByUsername: String
    )

    /**
     * DTO for the detailed view of a single product variant.
     */
    data class ProductVariantInfoDTO(
        val id: Long,
        val sku: String,
        val price: BigDecimal,
        val costPrice: BigDecimal?,
        val weight: BigDecimal?,
        val attributes: String?,
        val createdAt: OffsetDateTime?,
        val updatedAt: OffsetDateTime?
    )

    /**
     * A DTO for displaying details of a single product variant.
     */
    data class ProductVariantDetailTransactionsDTO(
        val id: Long,
        val sku: String,
        val price: BigDecimal,
        val costPrice: BigDecimal?,
        val weight: BigDecimal?,
        val attributes: String?,
        val inventoryTransactions: List<InventoryTransactionInfoDTO>
    )

    /**
     * A DTO for displaying a single inventory transaction.
     */
    data class InventoryTransactionInfoDTO(
        val type: InventoryTransactionType,
        val quantity: Int,
        val notes: String?,
        val createdAt: OffsetDateTime?
    )

}

