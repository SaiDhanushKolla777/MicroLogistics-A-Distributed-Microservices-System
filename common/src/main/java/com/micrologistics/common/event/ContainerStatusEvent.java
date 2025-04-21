package com.micrologistics.common.event;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event that is published when a container's status changes.
 * Used for communication between the container management service and other services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerStatusEvent {
    
    private String id;
    
    private String containerNumber;
    
    private String destination;
    
    private Double currentWeight;
    
    private Double maxWeight;
    
    private Double currentVolume;
    
    private Double maxVolume;
    
    private Integer itemCount;
    
    private String status;
    
    private List<String> itemIds;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Container status constants
     */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_LOADING = "LOADING";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_DISPATCHED = "DISPATCHED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    
    /**
     * Calculate the weight utilization percentage of the container.
     * 
     * @return The weight utilization as a percentage
     */
    public Double getWeightUtilizationPercentage() {
        if (maxWeight == null || maxWeight == 0 || currentWeight == null) {
            return 0.0;
        }
        return (currentWeight / maxWeight) * 100.0;
    }
    
    /**
     * Calculate the volume utilization percentage of the container.
     * 
     * @return The volume utilization as a percentage
     */
    public Double getVolumeUtilizationPercentage() {
        if (maxVolume == null || maxVolume == 0 || currentVolume == null) {
            return 0.0;
        }
        return (currentVolume / maxVolume) * 100.0;
    }
}
