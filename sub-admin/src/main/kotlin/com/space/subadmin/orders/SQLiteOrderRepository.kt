package com.space.subadmin.orders

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant


interface OrderRepository {

    fun findTopSellingProducts(limit: Int): List<Array<Any>>
    fun findDailyRevenueAfter(since: Instant): List<Array<Any>>
    fun findOrdersAfter(since: Instant): List<Order>
    fun findAllWithCustomer(): List<Order>

}

@Repository
interface SQLiteOrderRepository : OrderRepository, JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.customer")
    override fun findAllWithCustomer(): List<Order>

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :since")
    override fun findOrdersAfter(since: Instant): List<Order>

    @Query(
        """
         SELECT CAST(o.orderDate AS DATE), SUM(o.totalAmount)
         FROM Order o
         WHERE o.orderDate >= :since
         GROUP BY CAST(o.orderDate AS DATE)
         ORDER BY CAST(o.orderDate AS DATE)
     """
    )
    override fun findDailyRevenueAfter(since: Instant): List<Array<Any>>

    @Query(
        """
         SELECT pv.product.name, SUM(oi.quantity), SUM(oi.lineTotal)
         FROM OrderItem oi JOIN oi.productVariant pv
         GROUP BY pv.product.name ORDER BY SUM(oi.lineTotal) DESC LIMIT :limit
     """
    )
    override fun findTopSellingProducts(limit: Int): List<Array<Any>>
}