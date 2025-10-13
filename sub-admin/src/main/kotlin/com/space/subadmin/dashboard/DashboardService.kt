package com.space.subadmin.dashboard

import com.space.subadmin.orders.OrderRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit

data class DashboardData(
    val totalRevenue: BigDecimal,
    val totalOrders: Long,
    val averageOrderValue: BigDecimal,
    val revenueTrend: Map<String, BigDecimal>,
    val topSellingProducts: List<TopSellingProduct>
)

data class TopSellingProduct(
    val productName: String,
    val totalQuantity: Long,
    val totalRevenue: BigDecimal
)

@Service
class DashboardService(private val orderRepository: OrderRepository) {

    fun getDashboardData(): DashboardData {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)
        val recentOrders = orderRepository.findOrdersAfter(thirtyDaysAgo)

        val totalRevenue = recentOrders.sumOf { it.totalAmount }
        val totalOrders = recentOrders.size.toLong()
        val averageOrderValue = if (totalOrders > 0) {
            totalRevenue.divide(BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val revenueTrend = orderRepository.findDailyRevenueAfter(thirtyDaysAgo)
            .associate { it["order_day"].toString() to it["revenue"] as BigDecimal }

        val topSellingProducts = orderRepository.findTopSellingProducts(5)
            .map {
                TopSellingProduct(
                    productName = it["name"] as String,
                    totalQuantity = (it["total_quantity"] as Number).toLong(),
                    totalRevenue = it["total_revenue"] as BigDecimal
                )
            }

        return DashboardData(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            averageOrderValue = averageOrderValue,
            revenueTrend = revenueTrend,
            topSellingProducts = topSellingProducts
        )
    }
}