package com.space.subadmin.products

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/products")
class ProductJteWebController(
    private val productService: ProductService
) {

    @GetMapping("/list")
    fun showProductList(model: Model): String {
        val productVariants = productService.getAllProductVariantsWithInventory()
        model.addAttribute("products", productVariants)
        return "products/products-list"
    }

    @GetMapping("/add")
    fun showAddProductForm(model: Model): String {
        model.addAttribute("productForm", ProductFormDTO())
        model.addAttribute("isEdit", false)
        return "products/product-editor"
    }

    @GetMapping("/edit/{productId}")
    fun showEditProductForm(@PathVariable productId: Long, model: Model): String {
        val productForm = productService.getProductFormById(productId) // Assuming this method exists
        model.addAttribute("productForm", productForm)
        model.addAttribute("isEdit", true)
        model.addAttribute("productId", productId)
        return "products/product-editor"
    }
}
