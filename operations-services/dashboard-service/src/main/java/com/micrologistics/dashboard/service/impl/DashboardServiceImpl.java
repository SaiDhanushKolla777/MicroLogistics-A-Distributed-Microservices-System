package com.micrologistics.dashboard.service.impl;

import com.micrologistics.dashboard.client.MetricsServiceClient;
import com.micrologistics.dashboard.dto.AlertDto;
import com.micrologistics.dashboard.dto.DashboardDto;
import com.micrologistics.dashboard.service.DashboardService;
import com.micrologistics.dashboard.visualization.ForecastingEngine;
import com.micrologistics.dashboard.visualization.HeatmapGenerator;
import com.micrologistics.dashboard.visualization.TrendAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MetricsServiceClient metricsServiceClient;
    private final HeatmapGenerator heatmapGenerator;
    private final TrendAnalyzer trendAnalyzer;
    private final ForecastingEngine forecastingEngine;
    private final AlertServiceImpl alertService;

    @Override
    public DashboardDto getCurrentDashboard() {
        // Fetch metrics and process them for dashboard visualization
        return DashboardDto.builder()
                .itemFlowMap(heatmapGenerator.generate())
                .trendReport(trendAnalyzer.analyzeTrends())
                .forecastReport(forecastingEngine.forecast())
                .alerts(alertService.getCurrentAlerts())
                .build();
    }

    @Override
    public List<AlertDto> getCurrentAlerts() {
        return alertService.getCurrentAlerts();
    }
}
