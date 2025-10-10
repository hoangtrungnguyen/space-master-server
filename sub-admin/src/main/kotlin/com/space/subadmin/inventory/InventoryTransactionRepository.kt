package com.space.subadmin.inventory

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface InventoryTransactionRepository : JpaRepository<InventoryTransaction, UUID>
