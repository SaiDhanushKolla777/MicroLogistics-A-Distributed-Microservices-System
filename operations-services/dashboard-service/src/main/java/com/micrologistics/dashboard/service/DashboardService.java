package com.micrologistics.dashboard.service;

import com.micrologistics.dashboard.dto.DashboardDto;
import com.micrologistics.dashboard.dto.AlertDto;

import java.util.List;

public interface DashboardService {
    DashboardDto getCurrentDashboard();
    List<AlertDto> getCurrentAlerts();
}
