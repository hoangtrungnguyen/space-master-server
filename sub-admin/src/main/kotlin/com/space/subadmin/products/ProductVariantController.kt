package com.space.subadmin.products

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/v1/product-variants")
class ProductVariantController(
    private val getAllProductVariants: GetAllProductVariants
) {

    @GetMapping
    fun getAll(): List<ProductAndVariantDTO> {
        return getAllProductVariants.execute()
    }
}
