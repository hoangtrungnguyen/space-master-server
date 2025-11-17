package com.space.subadmin.dashboard

import java.math.BigDecimal

data class DashboardDataDto(
    val totalRevenue: BigDecimal,
    val totalOrders: Long,
    val averageOrderValue: BigDecimal,
    val revenueTrend: String,
    val topSellingProducts: List<TopSellingProduct>
)

