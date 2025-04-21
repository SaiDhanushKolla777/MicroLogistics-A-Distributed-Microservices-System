package com.micrologistics.metrics.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for operational metrics in the logistics system.
 * Represents persistent metric data for operational analysis.
 */
@Entity
@Table(name = "operational_metrics", 
       indexes = {
           @Index(name = "idx_metric_name", columnList = "metric_name"),
           @Index(name = "idx_service_id", columnList = "service_id"),
           @Index(name = "idx_timestamp", columnList = "timestamp"),
           @Index(name = "idx_metric_name_timestamp", columnList = "metric_name,timestamp")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @Column(name = "service_id", nullable = false)
    private String serviceId;
    
    @Column(name = "resource_id")
    private String resourceId;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Metrics type constants
     */
    public static final String METRIC_THROUGHPUT = "throughput";
    public static final String METRIC_LATENCY = "latency";
    public static final String METRIC_ERROR_RATE = "error_rate";
    public static final String METRIC_UTILIZATION = "utilization";
    public static final String METRIC_ITEM_COUNT = "item_count";
    public static final String METRIC_CONTAINER_COUNT = "container_count";
    public static final String METRIC_ROUTE_COUNT = "route_count";
    
    /**
     * Service ID constants
     */
    public static final String SERVICE_ITEM_REGISTRATION = "item-registration";
    public static final String SERVICE_ROUTING = "routing-service";
    public static final String SERVICE_CONTAINER = "container-management";
    public static final String SERVICE_GATEWAY = "api-gateway";
    
    /**
     * Unit constants
     */
    public static final String UNIT_COUNT = "count";
    public static final String UNIT_MILLISECONDS = "ms";
    public static final String UNIT_PERCENTAGE = "percent";
    public static final String UNIT_ITEMS_PER_SECOND = "items/sec";
    public static final String UNIT_REQUESTS_PER_SECOND = "req/sec";
}
