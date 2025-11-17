package com.space.subadmin.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.space.subadmin.customers.CustomerRepository
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
    val topSellingProducts: List<TopSellingProduct>,
    val grossProfitMargin: BigDecimal,
    val newCustomers: Long,
    val customerRetentionRate: BigDecimal
)

data class TopSellingProduct(
    val productName: String,
    val totalQuantity: Long,
    val totalRevenue: BigDecimal
)

@Service
class DashboardService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val dashboardMapper: DashboardMapper
) {

    fun getDashboardUiDto(): DashboardUiDto {
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

        val newCustomers = customerRepository.countByCreatedAtAfter(thirtyDaysAgo)

        // TODO: Implement actual calculation for Gross Profit Margin and Customer Retention Rate
        val grossProfitMargin = BigDecimal("0.5") // Mocked value: 50%
        val customerRetentionRate = BigDecimal("0.85") // Mocked value: 85%

        val dashboardData = DashboardData(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            averageOrderValue = averageOrderValue,
            revenueTrend = revenueTrend,
            topSellingProducts = topSellingProducts,
            grossProfitMargin = grossProfitMargin,
            newCustomers = newCustomers,
            customerRetentionRate = customerRetentionRate
        )

        return dashboardMapper.toUiDto(dashboardData)
    }
}