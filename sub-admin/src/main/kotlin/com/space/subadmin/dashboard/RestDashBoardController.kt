package com.space.subadmin.dashboard

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class RestDashBoardController(
    private val dashboardService: DashboardService,
) {

    @GetMapping("/v2")
    fun getDashboardDataV2(): DashBoardUiDtoV2 {
        val dashboardData = dashboardService.getDashboardDataV2()
        return dashboardData.toUiDto()
    }
}