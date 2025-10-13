package com.space.subadmin.products

/**
 * A DTO to represent a product variant with its inventory status
 * for the main product list.
 */
data class ProductVariantInventoryDTO(
    val productId: Long,
    val productName: String,
    val brandName: String?,
    val categoryName: String?,
    val variantId: Long,
    val variantSku: String,
    val quantityOnHand: Int?,
    val isActive: Boolean
)
