package com.micrologistics.metrics.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.entity.TimeSeriesData;
import com.micrologistics.metrics.repository.OperationalMetricsRepository;
import com.micrologistics.metrics.repository.TimeSeriesRepository;
import com.micrologistics.metrics.service.MetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the MetricsService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsServiceImpl implements MetricsService {
    
    private final OperationalMetricsRepository metricsRepository;
    private final TimeSeriesRepository timeSeriesRepository;
    
    @Value("${metrics.environment:development}")
    private String environment;
    
    @Value("${metrics.region:us-east-1}")
    private String region;
    
    @Value("${metrics.availability-zone:us-east-1a}")
    private String availabilityZone;

    @Override
    @Transactional
    public OperationalMetrics recordMetric(OperationalMetrics metrics) {
        log.debug("Recording metric: {}, value: {}, service: {}", 
                metrics.getMetricName(), metrics.getValue(), metrics.getServiceId());
        
        if (metrics.getTimestamp() == null) {
            metrics.setTimestamp(LocalDateTime.now());
        }
        
        // Save to relational database
        OperationalMetrics savedMetrics = metricsRepository.save(metrics);
        
        // Also save to time series database if applicable
        TimeSeriesData timeSeriesData = TimeSeriesData.fromOperationalMetrics(
                savedMetrics, environment, region, availabilityZone);
        recordTimeSeriesData(timeSeriesData);
        
        return savedMetrics;
    }

    @Override
    public boolean recordTimeSeriesData(TimeSeriesData timeSeriesData) {
        return timeSeriesRepository.save(timeSeriesData);
    }

    @Override
    public List<OperationalMetrics> getMetricsByName(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.findByMetricNameAndTimestampBetween(metricName, startTime, endTime);
    }

    @Override
    public List<OperationalMetrics> getMetricsByServiceAndName(
            String serviceId, String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.findByServiceIdAndMetricNameAndTimestampBetween(
                serviceId, metricName, startTime, endTime);
    }

    @Override
    public List<OperationalMetrics> getMetricsByService(String serviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.findByServiceIdAndTimestampBetween(serviceId, startTime, endTime);
    }

    @Override
    public List<OperationalMetrics> getMetricsByResource(
            String resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.findByResourceIdAndTimestampBetween(resourceId, startTime, endTime);
    }

    @Override
    public Page<OperationalMetrics> getMetricsByTimeRange(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return metricsRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    @Override
    public Double getAverageMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.getAverageValueByMetricNameAndTimeRange(metricName, startTime, endTime);
    }

    @Override
    public Double getMaxMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.getMaxValueByMetricNameAndTimeRange(metricName, startTime, endTime);
    }

    @Override
    public Double getMinMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return metricsRepository.getMinValueByMetricNameAndTimeRange(metricName, startTime, endTime);
    }

    @Override
    public Map<String, Double> getAverageMetricValueByService(
            String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> results = metricsRepository.getAverageValueByServiceAndMetricNameAndTimeRange(
                metricName, startTime, endTime);
        
        Map<String, Double> serviceAverages = new HashMap<>();
        for (Object[] result : results) {
            String serviceId = (String) result[0];
            Double avgValue = (Double) result[1];
            serviceAverages.put(serviceId, avgValue);
        }
        
        return serviceAverages;
    }

    @Override
    public List<OperationalMetrics> getLatestMetrics(int limit) {
        return metricsRepository.getLatestMetrics(limit);
    }

    @Override
    public Map<LocalDateTime, Double> getMetricTimeSeries(
            String metricName, LocalDateTime startTime, LocalDateTime endTime, int intervalMinutes) {
        List<Object[]> results = metricsRepository.getMetricTimeSeries(
                metricName, startTime, endTime, intervalMinutes);
        
        Map<LocalDateTime, Double> timeSeries = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        for (Object[] result : results) {
            String timestampStr = result[0].toString();
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
            Double avgValue = (Double) result[1];
            timeSeries.put(timestamp, avgValue);
        }
        
        return timeSeries;
    }

    @Override
    public Map<String, Object> getSystemHealthMetrics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        Map<String, Object> healthMetrics = new HashMap<>();
        
        // Get average throughput for the last hour
        Double avgThroughput = getAverageMetricValue(
                OperationalMetrics.METRIC_THROUGHPUT, oneHourAgo, now);
        healthMetrics.put("averageThroughput", avgThroughput != null ? avgThroughput : 0.0);
        
        // Get average latency for the last hour
        Double avgLatency = getAverageMetricValue(
                OperationalMetrics.METRIC_LATENCY, oneHourAgo, now);
        healthMetrics.put("averageLatency", avgLatency != null ? avgLatency : 0.0);
        
        // Get average error rate for the last hour
        Double avgErrorRate = getAverageMetricValue(
                OperationalMetrics.METRIC_ERROR_RATE, oneHourAgo, now);
        healthMetrics.put("averageErrorRate", avgErrorRate != null ? avgErrorRate : 0.0);
        
        // Get average utilization for the last hour
        Double avgUtilization = getAverageMetricValue(
                OperationalMetrics.METRIC_UTILIZATION, oneHourAgo, now);
        healthMetrics.put("averageUtilization", avgUtilization != null ? avgUtilization : 0.0);
        
        // Get metrics by service
        Map<String, Double> throughputByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_THROUGHPUT, oneHourAgo, now);
        healthMetrics.put("throughputByService", throughputByService);
        
        Map<String, Double> latencyByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_LATENCY, oneHourAgo, now);
        healthMetrics.put("latencyByService", latencyByService);
        
        Map<String, Double> errorRateByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_ERROR_RATE, oneHourAgo, now);
        healthMetrics.put("errorRateByService", errorRateByService);
        
        // Add timestamp
        healthMetrics.put("timestamp", now);
        
        return healthMetrics;
    }

    @Override
    public Map<String, Object> getThroughputReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // Get throughput metrics
        List<OperationalMetrics> throughputMetrics = getMetricsByName(
                OperationalMetrics.METRIC_THROUGHPUT, startTime, endTime);
        
        // Get average throughput
        Double avgThroughput = getAverageMetricValue(
                OperationalMetrics.METRIC_THROUGHPUT, startTime, endTime);
        
        // Get max throughput
        Double maxThroughput = getMaxMetricValue(
                OperationalMetrics.METRIC_THROUGHPUT, startTime, endTime);
        
        // Get throughput by service
        Map<String, Double> throughputByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_THROUGHPUT, startTime, endTime);
        
        // Get throughput time series
        Map<LocalDateTime, Double> throughputTimeSeries = getMetricTimeSeries(
                OperationalMetrics.METRIC_THROUGHPUT, startTime, endTime, 5); // 5-minute intervals
        
        // Add to report
        report.put("metrics", throughputMetrics);
        report.put("averageThroughput", avgThroughput != null ? avgThroughput : 0.0);
        report.put("maxThroughput", maxThroughput != null ? maxThroughput : 0.0);
        report.put("throughputByService", throughputByService);
        report.put("timeSeries", throughputTimeSeries);
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        
        return report;
    }

    @Override
    public Map<String, Object> getLatencyReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // Get latency metrics
        List<OperationalMetrics> latencyMetrics = getMetricsByName(
                OperationalMetrics.METRIC_LATENCY, startTime, endTime);
        
        // Get average latency
        Double avgLatency = getAverageMetricValue(
                OperationalMetrics.METRIC_LATENCY, startTime, endTime);
        
        // Get max latency
        Double maxLatency = getMaxMetricValue(
                OperationalMetrics.METRIC_LATENCY, startTime, endTime);
        
        // Get latency by service
        Map<String, Double> latencyByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_LATENCY, startTime, endTime);
        
        // Get latency time series
        Map<LocalDateTime, Double> latencyTimeSeries = getMetricTimeSeries(
                OperationalMetrics.METRIC_LATENCY, startTime, endTime, 5); // 5-minute intervals
        
        // Add to report
        report.put("metrics", latencyMetrics);
        report.put("averageLatency", avgLatency != null ? avgLatency : 0.0);
        report.put("maxLatency", maxLatency != null ? maxLatency : 0.0);
        report.put("latencyByService", latencyByService);
        report.put("timeSeries", latencyTimeSeries);
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        
        return report;
    }

    @Override
    public Map<String, Object> getErrorRateReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // Get error rate metrics
        List<OperationalMetrics> errorRateMetrics = getMetricsByName(
                OperationalMetrics.METRIC_ERROR_RATE, startTime, endTime);
        
        // Get average error rate
        Double avgErrorRate = getAverageMetricValue(
                OperationalMetrics.METRIC_ERROR_RATE, startTime, endTime);
        
        // Get max error rate
        Double maxErrorRate = getMaxMetricValue(
                OperationalMetrics.METRIC_ERROR_RATE, startTime, endTime);
        
        // Get error rate by service
        Map<String, Double> errorRateByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_ERROR_RATE, startTime, endTime);
        
        // Get error rate time series
        Map<LocalDateTime, Double> errorRateTimeSeries = getMetricTimeSeries(
                OperationalMetrics.METRIC_ERROR_RATE, startTime, endTime, 5); // 5-minute intervals
        
        // Add to report
        report.put("metrics", errorRateMetrics);
        report.put("averageErrorRate", avgErrorRate != null ? avgErrorRate : 0.0);
        report.put("maxErrorRate", maxErrorRate != null ? maxErrorRate : 0.0);
        report.put("errorRateByService", errorRateByService);
        report.put("timeSeries", errorRateTimeSeries);
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        
        return report;
    }

    @Override
    public Map<String, Object> getUtilizationReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // Get utilization metrics
        List<OperationalMetrics> utilizationMetrics = getMetricsByName(
                OperationalMetrics.METRIC_UTILIZATION, startTime, endTime);
        
        // Get average utilization
        Double avgUtilization = getAverageMetricValue(
                OperationalMetrics.METRIC_UTILIZATION, startTime, endTime);
        
        // Get max utilization
        Double maxUtilization = getMaxMetricValue(
                OperationalMetrics.METRIC_UTILIZATION, startTime, endTime);
        
        // Get utilization by service
        Map<String, Double> utilizationByService = getAverageMetricValueByService(
                OperationalMetrics.METRIC_UTILIZATION, startTime, endTime);
        
        // Get utilization time series
        Map<LocalDateTime, Double> utilizationTimeSeries = getMetricTimeSeries(
                OperationalMetrics.METRIC_UTILIZATION, startTime, endTime, 5); // 5-minute intervals
        
        // Add to report
        report.put("metrics", utilizationMetrics);
        report.put("averageUtilization", avgUtilization != null ? avgUtilization : 0.0);
        report.put("maxUtilization", maxUtilization != null ? maxUtilization : 0.0);
        report.put("utilizationByService", utilizationByService);
        report.put("timeSeries", utilizationTimeSeries);
        report.put("startTime", startTime);
        report.put("endTime", endTime);
        
        return report;
    }
}
