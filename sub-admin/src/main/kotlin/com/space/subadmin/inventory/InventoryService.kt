package com.space.subadmin.inventory

import com.space.subadmin.db.Inventory
import com.space.subadmin.db.InventoryTransaction
import com.space.subadmin.db.InventoryTransactionType
import com.space.subadmin.exception.OverQuantityException
import com.space.subadmin.products.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * A DTO to represent the inventory status of a product variant for the UI.
 */
data class InventoryStatusDTO(
    val productVariantId: Long,
    val productVariantSku: String,
    val quantityOnHand: Int?,
    val lastRestockedAt: OffsetDateTime?,
    val hasInventoryRecord: Boolean
)

@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val inventoryTransactionRepository: InventoryTransactionRepository,
    private val productVariantRepository: ProductVariantRepository
) {

    /**
     * Gathers the inventory status for all product variants.
     * This method ensures that every product variant is represented, even if it
     * has no corresponding inventory record yet.
     *
     * @return A list of DTOs representing the inventory status of each product.
     */
    fun getInventoryStatusForAllProducts(): List<InventoryStatusDTO> {
        val allVariants = productVariantRepository.findAll()
        val allInventories = inventoryRepository.findAll().associateBy { it.productVariant.id }

        return allVariants.map { variant ->
            val inventory = allInventories[variant.id]
            InventoryStatusDTO(
                productVariantId = variant.id!!,
                productVariantSku = variant.sku,
                quantityOnHand = inventory?.quantityOnHand,
                lastRestockedAt = inventory?.lastRestockedAt,
                hasInventoryRecord = inventory != null
            )
        }
    }

    @Transactional
    fun addQuantity(productVariantId: Long, quantity: Int, notes: String? = null): Inventory {
        val inventory = inventoryRepository.findByProductVariantId(productVariantId) ?: run {
            val productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow { IllegalArgumentException("Product variant not found") }
            // Correctly instantiate Inventory and set the lateinit property
            val newInventory = Inventory()
            newInventory.productVariant = productVariant
            newInventory // return the new inventory
        }

        inventory.quantityOnHand += quantity
        val savedInventory = inventoryRepository.save(inventory)

        // Correctly instantiate InventoryTransaction and set the lateinit property
        val transaction = InventoryTransaction(
            quantity = quantity,
            type = InventoryTransactionType.restock,
            notes = notes
        )
        transaction.inventory = savedInventory
        inventoryTransactionRepository.save(transaction)

        return savedInventory
    }

    @Transactional
    fun subtractQuantity(productVariantId: Long, quantity: Int, notes: String? = null): Inventory {
        val inventory = inventoryRepository.findByProductVariantId(productVariantId)
            ?: throw IllegalArgumentException("Inventory not found for product variant")

        if (inventory.quantityOnHand < quantity) {
            // Throw the enhanced exception with raw data
            throw OverQuantityException(
                message = "Attempted to subtract $quantity but only ${inventory.quantityOnHand} available.",
                productVariantId = productVariantId,
                quantityOnHand = inventory.quantityOnHand,
                quantityToSubtract = quantity
            )
        }

        inventory.quantityOnHand -= quantity
        val savedInventory = inventoryRepository.save(inventory)

        // Correctly instantiate InventoryTransaction and set the lateinit property
        val transaction = InventoryTransaction(
            quantity = quantity,
            type = InventoryTransactionType.adjustment, // Or sale, depending on context
            notes = notes
        )
        transaction.inventory = savedInventory
        inventoryTransactionRepository.save(transaction)

        return savedInventory
    }
}
