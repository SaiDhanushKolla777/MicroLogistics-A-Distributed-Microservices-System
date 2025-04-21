package com.micrologistics.metrics.analyzer;

import com.micrologistics.metrics.entity.OperationalMetrics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyzes metrics to detect processing bottlenecks.
 */
@Component
public class BottleneckDetector {

    /**
     * Finds metric types or services with highest average latency.
     */
    public String findWorstLatencyService(List<OperationalMetrics> metrics) {
        Map<String, Double> avgLatencyByService = metrics.stream()
                .filter(m -> "latency".equalsIgnoreCase(m.getMetricName()))
                .collect(Collectors.groupingBy(
                        OperationalMetrics::getServiceId,
                        Collectors.averagingDouble(OperationalMetrics::getValue)
                ));

        return avgLatencyByService.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    /**
     * Detects if there is a bottleneck by checking if latency is above threshold.
     */
    public boolean isBottleneck(List<OperationalMetrics> metrics, double latencyThreshold) {
        return metrics.stream()
                .filter(m -> "latency".equalsIgnoreCase(m.getMetricName()))
                .anyMatch(m -> m.getValue() > latencyThreshold);
    }
}
