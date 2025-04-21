package com.micrologistics.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private Map<String, Integer> itemFlowMap;
    private List<String> trendReport;
    private List<String> forecastReport;
    private List<AlertDto> alerts;
}
