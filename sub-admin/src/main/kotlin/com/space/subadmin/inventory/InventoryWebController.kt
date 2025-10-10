package com.space.subadmin.inventory

import com.space.subadmin.products.ProductVariantRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/inventory")
class InventoryWebController(
    private val inventoryService: InventoryService,
    private val productVariantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository
) {

    @GetMapping
    fun listInventory(model: Model): String {
        val inventoryList = inventoryRepository.findAll()
        model.addAttribute("inventoryList", inventoryList)
        return "inventory/index"
    }

    @GetMapping("/adjust/{productVariantId}")
    fun showAdjustForm(@PathVariable productVariantId: Long, model: Model): String {
        val productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow { IllegalArgumentException("Product variant not found") }
        val inventory = inventoryRepository.findByProductVariantId(productVariantId)
        model.addAttribute("productVariant", productVariant)
        model.addAttribute("inventory", inventory)
        return "inventory/adjust"
    }

    @PostMapping("/adjust")
    fun adjustInventory(
        @RequestParam productVariantId: Long,
        @RequestParam quantity: Int,
        @RequestParam type: String,
        @RequestParam notes: String?
    ): String {
        if (type == "add") {
            inventoryService.addQuantity(productVariantId, quantity, notes)
        } else if (type == "subtract") {
            inventoryService.subtractQuantity(productVariantId, quantity, notes)
        }
        return "redirect:/inventory"
    }
}
