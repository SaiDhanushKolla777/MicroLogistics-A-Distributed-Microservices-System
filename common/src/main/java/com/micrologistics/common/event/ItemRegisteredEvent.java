package com.micrologistics.common.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event that is published when a new item is registered in the system.
 * Used for communication between the item registration service and other services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRegisteredEvent {
    
    private String id;
    
    private String trackingId;
    
    private String description;
    
    private Double weight;
    
    private Double length;
    
    private Double width;
    
    private Double height;
    
    private String destination;
    
    private Integer priority;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Calculate the volume of the item.
     * 
     * @return The volume in cubic units
     */
    public Double getVolume() {
        if (length != null && width != null && height != null) {
            return length * width * height;
        }
        return null;
    }
}
