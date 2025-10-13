package com.space.subadmin.products

import java.time.OffsetDateTime

/**
 * A DTO for the product variant detail page, combining parent product info
 * with the details of a single, specific variant.
 */
data class ProductAndVariantDetailDTO(
    val product: ProductInfo,
    val variant: ProductVariantDetailDTO
) {
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
}
