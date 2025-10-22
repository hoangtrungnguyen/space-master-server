package com.space.subadmin.products

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/product-variants")
class ProductVariantWebController(
    private val productService: ProductService
) {

    @GetMapping("/add")
    fun addProductVariantPage(model: Model): String {
        model.addAttribute("variantForm", ProductVariantFormDTO())
        val products = productService.findAllProducts()
        model.addAttribute("products", products)
        return "products/add-product-variant"
    }

    @PostMapping("/add")
    fun addProductVariant(@ModelAttribute("variantForm") dto: ProductVariantFormDTO): String {
        productService.addVariantToProduct(dto)
        return "redirect:/products"
    }
}
