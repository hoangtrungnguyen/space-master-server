package com.space.subadmin.dashboard

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class DashBoardController(private val dashboardService: DashboardService) {
    @GetMapping("")
    fun showDashboard(model: Model): String {
        model.addAttribute("dashboardData", dashboardService.getDashboardData())
        return "index"
    }
}
