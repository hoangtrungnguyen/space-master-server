package com.space.subadmin.inventory

import com.space.subadmin.products.ProductVariantRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * A form-backing object for both inventory adjustments and initializations.
 */
data class InventoryAdjustmentForm(
    val productVariantId: Long = 0,
    val quantity: Int = 0,
    val notes: String? = null,
    val type: String = "add" // 'add' or 'subtract'
)

@Controller
@RequestMapping("/inventory")
class InventoryWebController(
    private val inventoryService: InventoryService,
    private val productVariantRepository: ProductVariantRepository
) {

    @GetMapping
    fun listInventory(model: Model): String {
        // Use the new service method to get status for all products
        model.addAttribute("inventoryList", inventoryService.getInventoryStatusForAllProducts())
        return "inventory/index"
    }

    @GetMapping("/adjust/{productVariantId}")
    fun showAdjustForm(@PathVariable productVariantId: Long, model: Model): String {
        val productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow { IllegalArgumentException("Product variant not found") }
        model.addAttribute("productVariant", productVariant)
        model.addAttribute("adjustmentForm", InventoryAdjustmentForm(productVariantId = productVariantId))
        return "inventory/adjust"
    }

    @PostMapping("/adjust")
    fun adjustInventory(form: InventoryAdjustmentForm): String {
        if (form.type == "add") {
            inventoryService.addQuantity(form.productVariantId, form.quantity, form.notes)
        } else if (form.type == "subtract") {
            inventoryService.subtractQuantity(form.productVariantId, form.quantity, form.notes)
        }
        return "redirect:/inventory"
    }

    /**
     * Shows a dedicated form for initializing the inventory for a product
     * that does not have an inventory record yet.
     */
    @GetMapping("/init/{productVariantId}")
    fun showInitForm(@PathVariable productVariantId: Long, model: Model): String {
        val productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow { IllegalArgumentException("Product variant not found") }
        model.addAttribute("productVariant", productVariant)
        // The form will be for adding quantity, so we pre-set the type
        model.addAttribute("adjustmentForm", InventoryAdjustmentForm(productVariantId = productVariantId, type = "add"))
        return "inventory/init-inventory"
    }
}
