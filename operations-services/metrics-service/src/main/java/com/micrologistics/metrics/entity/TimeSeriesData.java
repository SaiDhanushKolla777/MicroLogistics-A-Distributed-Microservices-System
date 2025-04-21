package com.micrologistics.metrics.entity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for time series data that will be stored in Amazon Timestream.
 * This is not a JPA entity but a model for the time series database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesData {
    
    private String metricName;
    private String serviceId;
    private String resourceId;
    private Double value;
    private String unit;
    private Instant timestamp;
    
    // Dimensions for time series data
    private String environment;
    private String region;
    private String availabilityZone;
    
    /**
     * Converts an OperationalMetrics entity to a TimeSeriesData object.
     * 
     * @param metrics The OperationalMetrics entity
     * @param environment The environment (e.g., prod, dev)
     * @param region The region (e.g., us-east-1)
     * @param availabilityZone The availability zone (e.g., us-east-1a)
     * @return A TimeSeriesData object
     */
    public static TimeSeriesData fromOperationalMetrics(
            OperationalMetrics metrics, 
            String environment, 
            String region, 
            String availabilityZone) {
        
        return TimeSeriesData.builder()
                .metricName(metrics.getMetricName())
                .serviceId(metrics.getServiceId())
                .resourceId(metrics.getResourceId())
                .value(metrics.getValue())
                .unit(metrics.getUnit())
                .timestamp(metrics.getTimestamp().toInstant(java.time.ZoneOffset.UTC))
                .environment(environment)
                .region(region)
                .availabilityZone(availabilityZone)
                .build();
    }
}
