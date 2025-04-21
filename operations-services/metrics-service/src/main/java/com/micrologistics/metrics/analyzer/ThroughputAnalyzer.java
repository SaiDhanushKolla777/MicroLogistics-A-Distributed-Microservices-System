package com.micrologistics.metrics.analyzer;

import com.micrologistics.metrics.entity.OperationalMetrics;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analyzes throughput metrics to help detect trends and performance patterns.
 */
@Component
public class ThroughputAnalyzer {

    /**
     * Calculates the average throughput from a list of metrics.
     */
    public double calculateAverageThroughput(List<OperationalMetrics> metrics) {
        return metrics.stream()
                .mapToDouble(OperationalMetrics::getValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Gets the peak throughput in a time window.
     */
    public double calculatePeakThroughput(List<OperationalMetrics> metrics) {
        return metrics.stream()
                .mapToDouble(OperationalMetrics::getValue)
                .max()
                .orElse(0.0);
    }

    /**
     * Returns time of day with highest throughput (if available).
     */
    public LocalDateTime getPeakThroughputTime(List<OperationalMetrics> metrics) {
        return metrics.stream()
                .max((a, b) -> Double.compare(a.getValue(), b.getValue()))
                .map(OperationalMetrics::getTimestamp)
                .orElse(null);
    }

}
