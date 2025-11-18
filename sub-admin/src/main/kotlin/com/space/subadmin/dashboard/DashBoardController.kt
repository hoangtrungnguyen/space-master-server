package com.space.subadmin.dashboard

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.stream.Collectors


@Controller
@RequestMapping("/")
class DashBoardController(private val dashboardService: DashboardService) {
    private val objectMapper = ObjectMapper()

    @GetMapping("")
    fun showDashboard(model: Model): String {

        val dashboardData = dashboardService.getDashboardDataV2()
        val dashboardUiDto = dashboardData.toUiDto()
        model.addAttribute("periodMetricDto", dashboardUiDto.periodMetrics)
        model.addAttribute("groupedChartData", dashboardUiDto.groupedChartData)
        model.addAttribute("days", 30)
        return "index_dashboard"
    }

    /**
     * 2. Renders ONLY the chart partial.
     * This is the endpoint our HTMX buttons will call.
     */
    @GetMapping("dashboard/chart-partial")
    @Throws(JsonProcessingException::class)
    fun getChartPartial(@RequestParam(defaultValue = "7") days: Int, model: Model): String {
        // 4. Serialize data to JSON strings to safely embed in the <script> tag
//        model.addAttribute("labelsJson", objectMapper.writeValueAsString(data.labels()))
//        model.addAttribute("dataJson", objectMapper.writeValueAsString(data.data()))

        val data = generateDummyData(30)

        model.addAttribute(
            "labelsJson",
            objectMapper.writeValueAsString(data.labels)
        )
        model.addAttribute("dataJson", objectMapper.writeValueAsString(data.data))

        model.addAttribute(
            "transactionLabels",
            objectMapper.writeValueAsString(data.labels)
        )

        model.addAttribute(
            "transactionData",
            objectMapper.writeValueAsString(data.data)
        )
        // Renders the partial template: /src/main/jte/includes/chart.kte
        return "dashboard/chart"
    }


    // A simple record to hold our chart data
    @JvmRecord
    private data class ChartData(val labels: List<String?>?, val data: List<Int?>?)

    private fun generateDummyData(days: Int): ChartData {
        // Create a list of size 'days', where 'i' is the index (0 to days-1)
        val labels = List(days) { i -> "Day ${i + 1}" }

        val data = List(days) {
            (Math.random() * 100).toInt() + if (days == 7) 50 else 200
        }

        return ChartData(labels, data)
    }
}
