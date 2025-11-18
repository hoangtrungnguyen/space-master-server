package com.space.subadmin.dashboard

import java.math.BigDecimal

data class DashBoardUiDtoV2(
    val groupedChartData: List<GroupedChartDataUiDto>,
    val periodMetrics: PeriodMetricsUiDto,
    val topSellingProducts: List<TopSellingProductUiDto>
)

data class TransactionUiDto(
    val date: String,
    val revenue: BigDecimal,
    val cogs: BigDecimal
)

data class PeriodMetricsUiDto (
    val totalRevenue: BigDecimal,
    val totalCogs: BigDecimal,
    val transactionCount: Int
)

data class GroupedChartDataUiDto(
    val date: String,
    val revenue: BigDecimal,
    val cogs: BigDecimal,
    val transactions: Int
)

data class TopSellingProductUiDto(
    val productName: String,
    val totalQuantity: String,
    val totalRevenue: String
)

