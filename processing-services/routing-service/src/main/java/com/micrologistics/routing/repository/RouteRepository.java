package com.micrologistics.routing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.micrologistics.routing.entity.Route;

/**
 * Repository interface for managing Route entities.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, String> {
    
    /**
     * Find a route by its tracking ID.
     * 
     * @param trackingId The tracking ID
     * @return An Optional containing the route if found
     */
    Optional<Route> findByTrackingId(String trackingId);
    
    /**
     * Find a route by its item ID.
     * 
     * @param itemId The item ID
     * @return An Optional containing the route if found
     */
    Optional<Route> findByItemId(String itemId);
    
    /**
     * Find routes by their status.
     * 
     * @param status The status to filter by
     * @return A list of routes with the specified status
     */
    List<Route> findByStatus(String status);
    
    /**
     * Find routes by their status with pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of routes with the specified status
     */
    Page<Route> findByStatus(String status, Pageable pageable);
    
    /**
     * Find routes by their current step.
     * 
     * @param currentStep The current step
     * @return A list of routes at the specified step
     */
    List<Route> findByCurrentStep(String currentStep);
    
    /**
     * Find routes containing a specific step in their route steps.
     * 
     * @param step The step to look for
     * @return A list of routes that include the specified step
     */
    @Query("SELECT r FROM Route r JOIN r.routeSteps steps WHERE steps = :step")
    List<Route> findByRouteStepsContaining(String step);
    
    /**
     * Count routes by status.
     * 
     * @param status The status to count
     * @return The number of routes with the specified status
     */
    long countByStatus(String status);
    
    /**
     * Find the average estimated time for routes by current step.
     * 
     * @return A list of current steps and their average estimated times
     */
    @Query("SELECT r.currentStep, AVG(r.estimatedTimeMinutes) FROM Route r GROUP BY r.currentStep")
    List<Object[]> findAverageEstimatedTimeByStep();
    
    /**
     * Get routes with completion time exceeding estimated time.
     * 
     * @return A list of delayed routes
     */
    @Query("SELECT r FROM Route r WHERE r.status = 'COMPLETED' AND r.updatedAt > r.estimatedCompletionTime")
    List<Route> findDelayedRoutes();
}
