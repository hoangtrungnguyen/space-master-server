package com.space.subadmin.inventory

import com.space.subadmin.db.Inventory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface InventoryRepository : JpaRepository<Inventory, UUID> {
    fun findByProductVariantId(productVariantId: Long): Inventory?
}
