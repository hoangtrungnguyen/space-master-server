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

data class DashboardDataV2(
    val dailyMetrics: List<DailyMetric>,
    val totalMetrics: TotalMetrics,
    val topSellingProducts: List<TopSellingProduct>
)

data class DailyMetric(
    val date: String,
    val revenue: BigDecimal,
    val cogs: BigDecimal,
    val transactions: Int
)

data class TotalMetrics(
    val totalRevenue: BigDecimal,
    val totalCogs: BigDecimal,
    val transactionCount: Int
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
) {

    fun getDashboardDataV2(): DashboardDataV2 {
        val since = Instant.now().minus(30, ChronoUnit.DAYS)
        val dailyRevenueData = orderRepository.findDailyRevenueAfter(since)
        val dailyCogsData = orderRepository.findDailyCogsAfter(since)
        val topSellingProductsData = orderRepository.findTopSellingProducts(5)

        val dailyMetrics = mergeRevenueAndCogs(dailyRevenueData, dailyCogsData)

        val totalMetrics = TotalMetrics(
            totalRevenue = dailyMetrics.sumOf { it.revenue },
            totalCogs = dailyMetrics.sumOf { it.cogs },
            transactionCount = dailyMetrics.sumOf { it.transactions }
        )

        val topProducts = topSellingProductsData.map {
            TopSellingProduct(
                productName = it["name"] as String,
                totalQuantity = (it["total_quantity"] as Number).toLong(),
                totalRevenue = BigDecimal.valueOf((it["total_revenue"] as Number).toDouble())
            )
        }

        return DashboardDataV2(
            dailyMetrics = dailyMetrics,
            totalMetrics = totalMetrics,
            topSellingProducts = topProducts
        )
    }

    private fun mergeRevenueAndCogs(
        revenueData: List<Map<String, Any>>,
        cogsData: List<Map<String, Any>>
    ): List<DailyMetric> {
        val revenueMap = revenueData.associate {
            (it["order_day"] as java.sql.Date).toLocalDate().toString() to (it["revenue"] as BigDecimal)
        }
        val cogsMap = cogsData.associate {
            (it["order_day"] as java.sql.Date).toLocalDate().toString() to (it["cogs"] as BigDecimal)
        }
        // In this context, we assume one order is one transaction
        val transactionMap = revenueData.associate {
            (it["order_day"] as java.sql.Date).toLocalDate().toString() to (it["revenue"] as BigDecimal).toInt()
        }


        val allDates = (revenueMap.keys + cogsMap.keys).toSortedSet()

        return allDates.map { date ->
            DailyMetric(
                date = date,
                revenue = revenueMap[date] ?: BigDecimal.ZERO,
                cogs = cogsMap[date] ?: BigDecimal.ZERO,
                transactions = transactionMap[date] ?: 0
            )
        }
    }
}