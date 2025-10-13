package com.space.subadmin.products

import java.math.BigDecimal

/**
 * DTO for the new, simplified product creation form.
 */
data class ProductFormDTO(
    var name: String = "",
    var description: String? = null,
    var brandId: String? = null,
    var categoryId: String? = null,
)

/**
 * DTO for the new "add variant" form.
 */
data class ProductVariantFormDTO(
    var productId: Long? = null,
    var sku: String = "",
    var price: BigDecimal = BigDecimal.ZERO,
    var costPrice: BigDecimal? = null,
    var weight: BigDecimal? = null
)
