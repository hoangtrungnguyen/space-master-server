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

}