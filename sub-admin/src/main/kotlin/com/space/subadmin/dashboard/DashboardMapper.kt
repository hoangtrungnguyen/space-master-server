package com.space.subadmin.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.util.Locale


private val objectMapper: ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

fun DashboardDataV2.toUiDto(): DashBoardUiDtoV2 {
    val groupedChartData = this.dailyMetrics.map {
        GroupedChartDataUiDto(
            date = it.date,
            revenue = it.revenue,
            cogs = it.cogs,
            transactions = it.transactions
        )
    }

    val periodMetrics = PeriodMetricsUiDto(
        totalRevenue = this.totalMetrics.totalRevenue,
        totalCogs = this.totalMetrics.totalCogs,
        transactionCount = this.totalMetrics.transactionCount
    )

    val topSellingProducts = this.topSellingProducts.map {
        TopSellingProductUiDto(
            productName = it.productName,
            totalQuantity = it.totalQuantity.toString(),
            totalRevenue = currencyFormatter.format(it.totalRevenue)
        )
    }

    return DashBoardUiDtoV2(
        groupedChartData = groupedChartData,
        periodMetrics = periodMetrics,
        topSellingProducts = topSellingProducts
    )
}
