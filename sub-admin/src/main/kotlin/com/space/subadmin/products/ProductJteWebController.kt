package com.space.subadmin.products

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/jte/products")
class ProductJteWebController(
    private val productService: ProductService
) {

    @GetMapping("/list")
    fun showProductList(model: Model): String {
        val productVariants = productService.getAllProductVariantsWithInventory()
        model.addAttribute("products", productVariants)
        return "products-list"
    }
}
