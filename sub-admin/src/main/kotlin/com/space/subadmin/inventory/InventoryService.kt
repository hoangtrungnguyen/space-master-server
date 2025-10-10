package com.space.subadmin.inventory

import com.space.subadmin.products.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val inventoryTransactionRepository: InventoryTransactionRepository,
    private val productVariantRepository: ProductVariantRepository
) {

    @Transactional
    fun addQuantity(productVariantId: Long, quantity: Int, notes: String? = null): Inventory {
        val inventory = inventoryRepository.findByProductVariantId(productVariantId) ?: run {
            val productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow { IllegalArgumentException("Product variant not found") }
            Inventory(productVariant = productVariant)
        }

        inventory.quantityOnHand += quantity
        val savedInventory = inventoryRepository.save(inventory)

        val transaction = InventoryTransaction(
            inventory = savedInventory,
            quantity = quantity,
            type = InventoryTransactionType.restock,
            notes = notes
        )
        inventoryTransactionRepository.save(transaction)

        return savedInventory
    }

    @Transactional
    fun subtractQuantity(productVariantId: Long, quantity: Int, notes: String? = null): Inventory {
        val inventory = inventoryRepository.findByProductVariantId(productVariantId)
            ?: throw IllegalArgumentException("Inventory not found for product variant")

        if (inventory.quantityOnHand < quantity) {
            throw IllegalArgumentException("Not enough quantity on hand")
        }

        inventory.quantityOnHand -= quantity
        val savedInventory = inventoryRepository.save(inventory)

        val transaction = InventoryTransaction(
            inventory = savedInventory,
            quantity = quantity,
            type = InventoryTransactionType.adjustment, // Or sale, depending on context
            notes = notes
        )
        inventoryTransactionRepository.save(transaction)

        return savedInventory
    }
}
