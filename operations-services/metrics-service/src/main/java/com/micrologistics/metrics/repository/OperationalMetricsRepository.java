package com.micrologistics.metrics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.micrologistics.metrics.entity.OperationalMetrics;

/**
 * Repository interface for managing OperationalMetrics entities.
 */
@Repository
public interface OperationalMetricsRepository extends JpaRepository<OperationalMetrics, String> {
    
    /**
     * Find metrics by name and time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> findByMetricNameAndTimestampBetween(
            String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find metrics by service ID, metric name, and time range.
     * 
     * @param serviceId The service ID
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> findByServiceIdAndMetricNameAndTimestampBetween(
            String serviceId, String metricName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find metrics by service ID and time range.
     * 
     * @param serviceId The service ID
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> findByServiceIdAndTimestampBetween(
            String serviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find metrics by resource ID and time range.
     * 
     * @param resourceId The resource ID
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of metrics
     */
    List<OperationalMetrics> findByResourceIdAndTimestampBetween(
            String resourceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find metrics by time range with pagination.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @param pageable Pagination information
     * @return A page of metrics
     */
    Page<OperationalMetrics> findByTimestampBetween(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Calculate the average value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The average value
     */
    @Query("SELECT AVG(m.value) FROM OperationalMetrics m WHERE m.metricName = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageValueByMetricNameAndTimeRange(
            @Param("metricName") String metricName, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Calculate the maximum value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The maximum value
     */
    @Query("SELECT MAX(m.value) FROM OperationalMetrics m WHERE m.metricName = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getMaxValueByMetricNameAndTimeRange(
            @Param("metricName") String metricName, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Calculate the minimum value of a metric over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return The minimum value
     */
    @Query("SELECT MIN(m.value) FROM OperationalMetrics m WHERE m.metricName = :metricName AND m.timestamp BETWEEN :startTime AND :endTime")
    Double getMinValueByMetricNameAndTimeRange(
            @Param("metricName") String metricName, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Calculate the average value of a metric for each service over a time range.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of service IDs and their average values
     */
    @Query("SELECT m.serviceId, AVG(m.value) FROM OperationalMetrics m WHERE m.metricName = :metricName AND m.timestamp BETWEEN :startTime AND :endTime GROUP BY m.serviceId")
    List<Object[]> getAverageValueByServiceAndMetricNameAndTimeRange(
            @Param("metricName") String metricName, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Get the latest metrics for each metric name.
     * 
     * @param limit The maximum number of records to return
     * @return A list of the latest metrics
     */
    @Query(value = 
            "SELECT * FROM operational_metrics m1 " +
            "WHERE m1.timestamp = (SELECT MAX(m2.timestamp) FROM operational_metrics m2 WHERE m2.metric_name = m1.metric_name) " +
            "ORDER BY m1.timestamp DESC LIMIT :limit", 
            nativeQuery = true)
    List<OperationalMetrics> getLatestMetrics(@Param("limit") int limit);
    
    /**
     * Get a time series of metric values aggregated by time interval.
     * 
     * @param metricName The metric name
     * @param startTime The start time
     * @param endTime The end time
     * @param intervalMinutes The interval in minutes
     * @return A list of timestamps and average values
     */
    @Query(value = 
            "SELECT DATE_TRUNC('hour', timestamp) + " +
            "INTERVAL ':intervalMinutes minutes' * DIV(EXTRACT(MINUTE FROM timestamp), :intervalMinutes) as time_bucket, " +
            "AVG(value) as avg_value " +
            "FROM operational_metrics " +
            "WHERE metric_name = :metricName AND timestamp BETWEEN :startTime AND :endTime " +
            "GROUP BY time_bucket " +
            "ORDER BY time_bucket", 
            nativeQuery = true)
    List<Object[]> getMetricTimeSeries(
            @Param("metricName") String metricName, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime,
            @Param("intervalMinutes") int intervalMinutes);
}
