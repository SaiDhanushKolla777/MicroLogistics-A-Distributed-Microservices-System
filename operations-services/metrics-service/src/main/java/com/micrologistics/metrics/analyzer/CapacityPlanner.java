package com.micrologistics.metrics.analyzer;

import com.micrologistics.metrics.entity.OperationalMetrics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Suggests capacity planning actions based on observed utilization.
 */
@Component
public class CapacityPlanner {

    /**
     * Determines if a service is approaching full capacity.
     */
    public boolean needsScaling(List<OperationalMetrics> metrics, double utilizationThreshold) {
        return metrics.stream()
                .filter(m -> "utilization".equalsIgnoreCase(m.getMetricName()))
                .anyMatch(m -> m.getValue() > utilizationThreshold);
    }

    /**
     * Returns services with average utilization above threshold.
     */
    public List<String> getServicesNeedingScaling(List<OperationalMetrics> metrics, double threshold) {
        Map<String, Double> avgUtilizationByService = metrics.stream()
                .filter(m -> "utilization".equalsIgnoreCase(m.getMetricName()))
                .collect(Collectors.groupingBy(
                        OperationalMetrics::getServiceId,
                        Collectors.averagingDouble(OperationalMetrics::getValue)
                ));

        return avgUtilizationByService.entrySet().stream()
                .filter(e -> e.getValue() > threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
