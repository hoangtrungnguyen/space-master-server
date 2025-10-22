package com.space.subadmin.inventory

import com.space.subadmin.db.InventoryTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface InventoryTransactionRepository : JpaRepository<InventoryTransaction, UUID> {

    /**
     * Finds all inventory transactions for a given list of product variant IDs,
     * ordered by the most recent transaction first.
     * This query efficiently fetches all transactions in a single database call.
     */
    @Query("""
        SELECT it FROM InventoryTransaction it
        JOIN FETCH it.inventory i
        WHERE i.productVariant.id IN :productVariantIds
        ORDER BY it.createdAt DESC
    """)
    fun findByProductVariantIdIn(productVariantIds: List<Long>): List<InventoryTransaction>
}
