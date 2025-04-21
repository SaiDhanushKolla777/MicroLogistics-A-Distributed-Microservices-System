package com.micrologistics.metrics.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.service.MetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for metrics.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {
    
    private final MetricsService metricsService;
    
    /**
     * Record a metric.
     * 
     * @param metrics The metric data
     * @return The recorded metric
     */
    @PostMapping
    public ResponseEntity<OperationalMetrics> recordMetric(@RequestBody OperationalMetrics metrics) {
        log.info("Received request to record metric: {}", metrics.getMetricName());
        OperationalMetrics recordedMetrics = metricsService.recordMetric(metrics);
        return ResponseEntity.ok(recordedMetrics);
    }
    
    /**
     * Get metrics by name and time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    @GetMapping("/{metricName}")
    public ResponseEntity<List<OperationalMetrics>> getMetricsByName(
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get metrics by name: {} from {} to {}", 
                metricName, startTime, endTime);
        
        List<OperationalMetrics> metrics = metricsService.getMetricsByName(metricName, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get metrics by service, name, and time range.
     * 
     * @param serviceId The service ID
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    @GetMapping("/service/{serviceId}/{metricName}")
    public ResponseEntity<List<OperationalMetrics>> getMetricsByServiceAndName(
            @PathVariable String serviceId,
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get metrics by service: {} and name: {} from {} to {}", 
                serviceId, metricName, startTime, endTime);
        
        List<OperationalMetrics> metrics = metricsService.getMetricsByServiceAndName(
                serviceId, metricName, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get average metric value.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The average value
     */
    @GetMapping("/average/{metricName}")
    public ResponseEntity<Double> getAverageMetricValue(
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get average value for metric: {} from {} to {}", 
                metricName, startTime, endTime);
        
        Double averageValue = metricsService.getAverageMetricValue(metricName, startTime, endTime);
        return ResponseEntity.ok(averageValue != null ? averageValue : 0.0);
    }
    
    /**
     * Get average metric value by service.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of service IDs to average values
     */
    @GetMapping("/average/{metricName}/by-service")
    public ResponseEntity<Map<String, Double>> getAverageMetricValueByService(
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get average value by service for metric: {} from {} to {}", 
                metricName, startTime, endTime);
        
        Map<String, Double> averageValues = metricsService.getAverageMetricValueByService(
                metricName, startTime, endTime);
        return ResponseEntity.ok(averageValues);
    }
    
    /**
     * Get metrics by time range with pagination.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @param page The page number
     * @param size The page size
     * @param sortBy The field to sort by
     * @param direction The sort direction
     * @return A page of metrics
     */
    @GetMapping
    public ResponseEntity<Page<OperationalMetrics>> getMetricsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("Received request to get metrics from {} to {} with pagination", startTime, endTime);
        
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<OperationalMetrics> metrics = metricsService.getMetricsByTimeRange(startTime, endTime, pageable);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get latest metrics for each metric name.
     * 
     * @param limit The maximum number of records to return
     * @return A list of the latest metrics
     */
    @GetMapping("/latest")
    public ResponseEntity<List<OperationalMetrics>> getLatestMetrics(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Received request to get latest metrics with limit: {}", limit);
        
        List<OperationalMetrics> metrics = metricsService.getLatestMetrics(limit);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get time series for a metric.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @param intervalMinutes The interval in minutes
     * @return A map of timestamps to average values
     */
    @GetMapping("/timeseries/{metricName}")
    public ResponseEntity<Map<LocalDateTime, Double>> getMetricTimeSeries(
            @PathVariable String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "5") int intervalMinutes) {
        
        log.info("Received request to get time series for metric: {} from {} to {} with interval: {} minutes", 
                metricName, startTime, endTime, intervalMinutes);
        
        Map<LocalDateTime, Double> timeSeries = metricsService.getMetricTimeSeries(
                metricName, startTime, endTime, intervalMinutes);
        return ResponseEntity.ok(timeSeries);
    }
    
    /**
     * Get system health metrics.
     * 
     * @return A map of health metrics
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealthMetrics() {
        log.info("Received request to get system health metrics");
        Map<String, Object> healthMetrics = metricsService.getSystemHealthMetrics();
        return ResponseEntity.ok(healthMetrics);
    }
    
    /**
     * Get throughput report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of throughput metrics
     */
    @GetMapping("/throughput/report")
    public ResponseEntity<Map<String, Object>> getThroughputReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get throughput report from {} to {}", startTime, endTime);
        
        Map<String, Object> report = metricsService.getThroughputReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get latency report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of latency metrics
     */
    @GetMapping("/latency/report")
    public ResponseEntity<Map<String, Object>> getLatencyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get latency report from {} to {}", startTime, endTime);
        
        Map<String, Object> report = metricsService.getLatencyReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get error rate report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of error rate metrics
     */
    @GetMapping("/error-rate/report")
    public ResponseEntity<Map<String, Object>> getErrorRateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get error rate report from {} to {}", startTime, endTime);
        
        Map<String, Object> report = metricsService.getErrorRateReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get utilization report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of utilization metrics
     */
    @GetMapping("/utilization/report")
    public ResponseEntity<Map<String, Object>> getUtilizationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("Received request to get utilization report from {} to {}", startTime, endTime);
        
        Map<String, Object> report = metricsService.getUtilizationReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }
}
