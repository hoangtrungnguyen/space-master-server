package com.space.subadmin.orders

import com.space.subadmin.db.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    /**
     * Finds all orders, eagerly fetching the associated customer.
     */
    @Query(
        """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.customer
        """
    )
    fun findAllWithCustomer(): List<Order>

    /**
     * Finds an order by its ID, eagerly fetching all related entities for the detail view.
     * This includes the customer, order items, product variants, and products.
     */
    @Query(
        """
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.customer
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.productVariant pv
        LEFT JOIN FETCH pv.product
        WHERE o.id = :id
        """
    )
    fun findOrderDetailById(id: Long): Optional<Order>

    /**
     * Finds orders created after a given date.
     * Note: This only loads the Order entity. Related entities like Customer must be loaded separately.
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :since")
    fun findOrdersAfter(since: Instant): List<Order>

    /**
     * Calculates the total daily revenue for orders placed after a given date.
     * Returns a List of Maps, where each map contains \"order_day\" and \"revenue\".
     */
    @Query(
        value = """
         SELECT CAST(order_date AS DATE) as order_day, SUM(total_amount) as revenue
         FROM orders
         WHERE order_date >= :since
         GROUP BY order_day
         ORDER BY order_day
     """, nativeQuery = true
    )
    fun findDailyRevenueAfter(since: Instant): List<Map<String, Any>>

    /**
     * Finds the top selling products based on revenue.
     * Returns a List of Maps, where each map contains \"name\", \"total_quantity\", and \"total_revenue\".
     */
    @Query(
        value = """
        SELECT p.name, SUM(oi.quantity) as total_quantity, SUM(oi.line_total) as total_revenue 
        FROM order_items oi 
        JOIN product_variants pv ON oi.product_variant_id = pv.id 
        JOIN products p ON pv.product_id = p.id 
        GROUP BY p.name 
        ORDER BY total_revenue DESC 
        LIMIT :limit
    """, nativeQuery = true
    )
    fun findTopSellingProducts(limit: Int): List<Map<String, Any>>
}
