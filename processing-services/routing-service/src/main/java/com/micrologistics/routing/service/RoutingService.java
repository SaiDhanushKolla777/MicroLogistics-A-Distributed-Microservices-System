package com.micrologistics.routing.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.common.event.ItemRegisteredEvent;

/**
 * Service interface for route management.
 */
public interface RoutingService {
    
    /**
     * Create a new route for an item.
     * 
     * @param itemId The item ID
     * @param trackingId The tracking ID
     * @param destination The destination
     * @param weight The weight
     * @param priority The priority (optional)
     * @return The created route
     */
    RouteDto createRoute(String itemId, String trackingId, String destination, Double weight, Integer priority);
    
    /**
     * Get a route by its ID.
     * 
     * @param id The route ID
     * @return The route
     */
    RouteDto getRouteById(String id);
    
    /**
     * Get a route by the tracking ID of its associated item.
     * 
     * @param trackingId The tracking ID
     * @return The route
     */
    RouteDto getRouteByTrackingId(String trackingId);
    
    /**
     * Get a route by the ID of its associated item.
     * 
     * @param itemId The item ID
     * @return The route
     */
    RouteDto getRouteByItemId(String itemId);
    
    /**
     * Get all routes, with optional pagination.
     * 
     * @param pageable Pagination information
     * @return A page of routes
     */
    Page<RouteDto> getAllRoutes(Pageable pageable);
    
    /**
     * Get routes by status, with optional pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of routes
     */
    Page<RouteDto> getRoutesByStatus(String status, Pageable pageable);
    
    /**
     * Update a route's current step.
     * 
     * @param id The route ID
     * @param step The new current step
     * @return The updated route
     */
    RouteDto updateRouteStep(String id, String step);
    
    /**
     * Process an item registered event by creating a route for it.
     * 
     * @param event The item registered event
     * @return The created route
     */
    RouteDto processItemRegisteredEvent(ItemRegisteredEvent event);
    
    /**
     * Get the current load status of all equipment.
     * 
     * @return A map of equipment to load
     */
    Map<String, Integer> getEquipmentLoadStatus();
    
    /**
     * Get the operational status of all equipment.
     * 
     * @return A map of equipment to operational status
     */
    Map<String, Boolean> getEquipmentOperationalStatus();
    
    /**
     * Update the operational status of equipment.
     * 
     * @param equipment The equipment name
     * @param isOperational Whether the equipment is operational
     */
    void updateEquipmentStatus(String equipment, boolean isOperational);
    
    /**
     * Get all available processing facilities.
     * 
     * @return List of facility names
     */
    List<String> getAllFacilities();
    
    /**
     * Get routes that are delayed (completion time exceeded estimated time).
     * 
     * @return List of delayed routes
     */
    List<RouteDto> getDelayedRoutes();
    
    /**
     * Get the average estimated time by step.
     * 
     * @return A map of step to average time
     */
    Map<String, Double> getAverageTimeByStep();
}
