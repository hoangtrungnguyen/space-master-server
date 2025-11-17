package com.space.subadmin.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.math.BigDecimal

@Controller
@RequestMapping("/")
class DashBoardController(private val dashboardService: DashboardService) {
    @GetMapping("")
    fun showDashboard(model: Model): String {
        model.addAttribute("dashboardData", dashboardService.getDashboardUiDto())
        return "index2"
    }
}

