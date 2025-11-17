package com.space.subadmin.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.stereotype.Component
import java.text.NumberFormat
import java.util.Locale

@Component
class DashboardMapper {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun toUiDto(dashboardData: DashboardData): DashboardUiDto {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        val percentFormatter = NumberFormat.getPercentInstance()

        val topSellingProducts = dashboardData.topSellingProducts.map {
            TopSellingProductUiDto(
                productName = it.productName,
                totalQuantity = "${it.totalQuantity} units sold",
                totalRevenue = currencyFormatter.format(it.totalRevenue)
            )
        }

        val revenueTrendSB = StringBuilder()
        val size = dashboardData.revenueTrend.size
        revenueTrendSB.append("{")

        dashboardData.revenueTrend.entries.toList() .forEachIndexed { index, it ->
            revenueTrendSB.append("\"${it.key}\"").append(":").append("\"${it.value}\"")
            if(index < size - 1){
                revenueTrendSB.append(", ")
            }
        }

        revenueTrendSB.append("}")

        return DashboardUiDto(
            grossProfitMargin = percentFormatter.format(dashboardData.grossProfitMargin),
            newCustomers = dashboardData.newCustomers.toString(),
            averageOrderValue = currencyFormatter.format(dashboardData.averageOrderValue),
            customerRetentionRate = percentFormatter.format(dashboardData.customerRetentionRate),
            revenueTrend = revenueTrendSB.toString(),
            topSellingProducts = topSellingProducts
        )
    }
}
