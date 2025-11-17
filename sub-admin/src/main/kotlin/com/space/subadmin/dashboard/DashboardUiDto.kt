package com.space.subadmin.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class TopSellingProductUiDto(
    val productName: String,
    val totalQuantity: String,
    val totalRevenue: String
)

data class DashboardUiDto(
    val grossProfitMargin: String,
    val newCustomers: String,
    val averageOrderValue: String,
    val customerRetentionRate: String,
    val revenueTrend: String,
    val topSellingProducts: List<TopSellingProductUiDto>
)
