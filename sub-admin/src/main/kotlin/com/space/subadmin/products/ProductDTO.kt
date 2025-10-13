package com.space.subadmin.products

import java.math.BigDecimal


/**
 * Data Transfer Object (DTO) for the "Edit Product" form.
 * This mirrors the structure of the form, including IDs for the product and
 * the specific variant being edited.
 */
data class ProductEditDTO(
    // IDs
    var productId: Long? = null,
    var variantId: Long? = null,

    // Product fields
    var name: String = "",
    var description: String? = null,
    var brandId: String? = null,
    var categoryId: String? = null,

    // ProductVariant fields
    var sku: String = "",
    var price: BigDecimal = BigDecimal.ZERO,
    var costPrice: BigDecimal? = null,
    var weight: BigDecimal? = null
)