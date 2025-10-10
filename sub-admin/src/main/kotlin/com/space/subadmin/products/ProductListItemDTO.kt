package com.space.subadmin.products

data class ProductListItemDTO(
    val id: Long?,
    val name: String,
    val brandName: String?,
    val categoryName: String?,
    val firstVariantSku: String?,
    val isActive: Boolean
)
