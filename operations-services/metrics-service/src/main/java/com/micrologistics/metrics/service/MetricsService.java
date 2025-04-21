package com.micrologistics.metrics.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.entity.TimeSeriesData;

/**
 * Service interface for metrics management.
 */
public interface MetricsService {
    
    /**
     * Record a metric data point.
     * 
     * @param metrics The metric data
     * @return The recorded metric
     */
    OperationalMetrics recordMetric(OperationalMetrics metrics);
    
    /**
     * Record a time series data point.
     * 
     * @param timeSeriesData The time series data
     * @return True if the operation was successful
     */
    boolean recordTimeSeriesData(TimeSeriesData timeSeriesData);
    
    /**
     * Get metrics by name and time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> getMetricsByName(String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get metrics by service, name, and time range.
     * 
     * @param serviceId The service ID
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> getMetricsByServiceAndName(
            String serviceId, String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get metrics by service and time range.
     * 
     * @param serviceId The service ID
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> getMetricsByService(String serviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get metrics by resource and time range.
     * 
     * @param resourceId The resource ID
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> getMetricsByResource(String resourceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get metrics by time range with pagination.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @param pageable Pagination information
     * @return A page of metrics
     */
    Page<OperationalMetrics> getMetricsByTimeRange(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Get the average value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The average value
     */
    Double getAverageMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the maximum value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The maximum value
     */
    Double getMaxMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the minimum value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The minimum value
     */
    Double getMinMetricValue(String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the average value of a metric for each service over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of service IDs to average values
     */
    Map<String, Double> getAverageMetricValueByService(
            String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the latest metrics for each metric name.
     * 
     * @param limit The maximum number of records to return
     * @return A list of the latest metrics
     */
    List<OperationalMetrics> getLatestMetrics(int limit);
    
    /**
     * Get a time series of metric values aggregated by time interval.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @param intervalMinutes The interval in minutes
     * @return A map of timestamps to average values
     */
    Map<LocalDateTime, Double> getMetricTimeSeries(
            String metricName, LocalDateTime startTime, LocalDateTime endTime, int intervalMinutes);
    
    /**
     * Get system health metrics.
     * 
     * @return A map of health metrics
     */
    Map<String, Object> getSystemHealthMetrics();
    
    /**
     * Get throughput report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of throughput metrics
     */
    Map<String, Object> getThroughputReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get latency report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of latency metrics
     */
    Map<String, Object> getLatencyReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get error rate report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of error rate metrics
     */
    Map<String, Object> getErrorRateReport(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get utilization report.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A map of utilization metrics
     */
    Map<String, Object> getUtilizationReport(LocalDateTime startTime, LocalDateTime endTime);
}
