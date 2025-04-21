package com.micrologistics.common.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for containers in the logistics system.
 * Used to transfer container data between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerDto {
    
    private String id;
    
    private String containerNumber;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Maximum weight is required")
    @Positive(message = "Maximum weight must be positive")
    private Double maxWeight;
    
    @NotNull(message = "Maximum volume is required")
    @Positive(message = "Maximum volume must be positive")
    private Double maxVolume;
    
    private Double currentWeight;
    
    private Double currentVolume;
    
    private Integer itemCount;
    
    private String status;
    
    private List<String> itemIds;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dispatchedAt;
    
    /**
     * Check if the container has available capacity for the given item.
     * 
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return True if the container can accommodate the item
     */
    public boolean hasCapacityFor(Double itemWeight, Double itemVolume) {
        if (currentWeight == null) {
            currentWeight = 0.0;
        }
        
        if (currentVolume == null) {
            currentVolume = 0.0;
        }
        
        return (currentWeight + itemWeight <= maxWeight) && 
               (currentVolume + itemVolume <= maxVolume);
    }
    
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
