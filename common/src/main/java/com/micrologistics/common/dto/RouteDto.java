package com.micrologistics.common.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for routes in the logistics system.
 * Used to transfer route data between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    
    private String id;
    
    private String itemId;
    
    private String trackingId;
    
    private List<String> routeSteps;
    
    private String currentStep;
    
    private String status;
    
    private Double estimatedTimeMinutes;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedCompletionTime;
    
    /**
     * Check if this is the final step in the route.
     * 
     * @return True if the current step is the last one in the route steps
     */
    public boolean isFinalStep() {
        if (routeSteps != null && !routeSteps.isEmpty() && currentStep != null) {
            return currentStep.equals(routeSteps.get(routeSteps.size() - 1));
        }
        return false;
    }
    
    /**
     * Get the next step in the route.
     * 
     * @return The next step, or null if this is the final step
     */
    public String getNextStep() {
        if (routeSteps != null && !routeSteps.isEmpty() && currentStep != null) {
            int currentIndex = routeSteps.indexOf(currentStep);
            if (currentIndex >= 0 && currentIndex < routeSteps.size() - 1) {
                return routeSteps.get(currentIndex + 1);
            }
        }
        return null;
    }
}
