package com.space.subadmin.inventory

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/inventory")
class InventoryRestController(
    private val inventoryService: InventoryService,
    private val inventoryRepository: InventoryRepository
) {

    @GetMapping
    fun getAllInventory(): ResponseEntity<Any> {
        val inventoryList = inventoryRepository.findAll()
        return ResponseEntity.ok(inventoryList)
    }

    @GetMapping("/{productVariantId}")
    fun getInventory(@PathVariable productVariantId: Long): ResponseEntity<Any> {
        val inventory = inventoryRepository.findByProductVariantId(productVariantId)
        return if (inventory != null) {
            ResponseEntity.ok(inventory)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/add")
    fun addInventory(@RequestBody request: AdjustInventoryRequest): ResponseEntity<Any> {
        inventoryService.addQuantity(request.productVariantId, request.quantity, request.notes)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/subtract")
    fun subtractInventory(@RequestBody request: AdjustInventoryRequest): ResponseEntity<Any> {
        inventoryService.subtractQuantity(request.productVariantId, request.quantity, request.notes)
        return ResponseEntity.ok().build()
    }
}

data class AdjustInventoryRequest(
    val productVariantId: Long,
    val quantity: Int,
    val notes: String?
)
