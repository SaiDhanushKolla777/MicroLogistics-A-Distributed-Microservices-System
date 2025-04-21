package com.micrologistics.routing.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for routes in the logistics system.
 * Represents the persistent data model for item routes.
 */
@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String itemId;
    
    @Column(nullable = false)
    private String trackingId;
    
    @ElementCollection
    @CollectionTable(name = "route_steps", joinColumns = @JoinColumn(name = "route_id"))
    @OrderColumn(name = "step_order")
    @Column(name = "step")
    private List<String> routeSteps;
    
    @Column(nullable = false)
    private String currentStep;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private Double estimatedTimeMinutes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    /**
     * Route status constants
     */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    
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
    
    /**
     * Get the index of the current step.
     * 
     * @return The index of the current step, or -1 if not found
     */
    public int getCurrentStepIndex() {
        if (routeSteps != null && currentStep != null) {
            return routeSteps.indexOf(currentStep);
        }
        return -1;
    }
    
    /**
     * Calculate the progress percentage of the route.
     * 
     * @return The progress as a percentage (0-100)
     */
    public double getProgressPercentage() {
        if (routeSteps == null || routeSteps.isEmpty()) {
            return 0.0;
        }
        
        int currentIndex = getCurrentStepIndex();
        if (currentIndex < 0) {
            return 0.0;
        }
        
        return ((double) (currentIndex + 1) / routeSteps.size()) * 100.0;
    }
}
