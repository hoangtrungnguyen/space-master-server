package com.space.subadmin.dashboard

import com.space.subadmin.orders.SQLiteOrderRepository
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
class DashboardService(private val SQLiteOrderRepository: SQLiteOrderRepository) {

    fun getDashboardData(): DashboardData {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)
        val recentOrders = SQLiteOrderRepository.findOrdersAfter(thirtyDaysAgo)

        val totalRevenue = recentOrders.sumOf { it.totalAmount }
        val totalOrders = recentOrders.size.toLong()
        val averageOrderValue = if (totalOrders > 0) {
            totalRevenue.divide(BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val revenueTrend = SQLiteOrderRepository.findDailyRevenueAfter(thirtyDaysAgo)
            .associate { it[0].toString() to it[1] as BigDecimal }

        val topSellingProducts = SQLiteOrderRepository.findTopSellingProducts(5)
            .map {
                TopSellingProduct(
                    productName = it[0] as String,
                    totalQuantity = it[1] as Long,
                    totalRevenue = it[2] as BigDecimal
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