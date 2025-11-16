package com.space.subadmin.orders

import com.space.subadmin.db.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DevOrderRepository : JpaRepository<Order, Long>
