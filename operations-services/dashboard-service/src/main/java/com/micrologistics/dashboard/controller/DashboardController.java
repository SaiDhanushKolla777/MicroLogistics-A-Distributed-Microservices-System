package com.micrologistics.dashboard.controller;

import com.micrologistics.dashboard.dto.DashboardDto;
import com.micrologistics.dashboard.dto.AlertDto;
import com.micrologistics.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDto getDashboard() {
        return dashboardService.getCurrentDashboard();
    }

    @GetMapping("/alerts")
    public List<AlertDto> getAlerts() {
        return dashboardService.getCurrentAlerts();
    }
}
