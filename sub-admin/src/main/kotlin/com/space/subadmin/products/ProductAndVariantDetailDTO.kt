package com.space.subadmin.products

import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * A DTO for the product variant detail page, combining parent product info
 * with the details of a single, specific variant.
 */
data class ProductAndVariantDTO(
    val product: ProductInfo,
    val variant: ProductVariantInfoDTO
) {
    /**
     * Nested DTO for parent product information.
     */
    data class ProductInfo(
        val id: Long,
        val name: String,
        val description: String?,
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
}


