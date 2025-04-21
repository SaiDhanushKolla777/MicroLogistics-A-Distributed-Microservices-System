package com.micrologistics.routing.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for route management.
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
public class RoutingController {
    
    private final RoutingService routingService;
    
    /**
     * Create a new route.
     * 
     * @param itemId The item ID
     * @param trackingId The tracking ID
     * @param destination The destination
     * @param weight The weight
     * @param priority The priority (optional)
     * @return The created route
     */
    @PostMapping
    public ResponseEntity<RouteDto> createRoute(
            @RequestParam String itemId,
            @RequestParam String trackingId,
            @RequestParam String destination,
            @RequestParam Double weight,
            @RequestParam(required = false) Integer priority) {
        
        log.info("Received request to create route for item: {}, tracking: {}", itemId, trackingId);
        RouteDto route = routingService.createRoute(itemId, trackingId, destination, weight, priority);
        return ResponseEntity.ok(route);
    }
    
    /**
     * Get a route by its ID.
     * 
     * @param id The route ID
     * @return The route
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteDto> getRouteById(@PathVariable String id) {
        log.info("Received request to get route by ID: {}", id);
        RouteDto route = routingService.getRouteById(id);
        return ResponseEntity.ok(route);
    }
    
    /**
     * Get a route by tracking ID.
     * 
     * @param trackingId The tracking ID
     * @return The route
     */
    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<RouteDto> getRouteByTrackingId(@PathVariable String trackingId) {
        log.info("Received request to get route by tracking ID: {}", trackingId);
        RouteDto route = routingService.getRouteByTrackingId(trackingId);
        return ResponseEntity.ok(route);
    }
    
    /**
     * Get a route by item ID.
     * 
     * @param itemId The item ID
     * @return The route
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<RouteDto> getRouteByItemId(@PathVariable String itemId) {
        log.info("Received request to get route by item ID: {}", itemId);
        RouteDto route = routingService.getRouteByItemId(itemId);
        return ResponseEntity.ok(route);
    }
    
    /**
     * Get all routes, with optional pagination and filtering.
     * 
     * @param page The page number (0-indexed)
     * @param size The page size
     * @param status Optional status filter
     * @param sortBy Property to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return A page of routes
     */
    @GetMapping
    public ResponseEntity<Page<RouteDto>> getRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("Received request to get routes, page: {}, size: {}, status: {}", page, size, status);
        
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<RouteDto> routes;
        if (status != null && !status.isEmpty()) {
            routes = routingService.getRoutesByStatus(status, pageable);
        } else {
            routes = routingService.getAllRoutes(pageable);
        }
        
        return ResponseEntity.ok(routes);
    }
    
    /**
     * Update a route's current step.
     * 
     * @param id The route ID
     * @param step The new current step
     * @return The updated route
     */
    @PutMapping("/{id}/step")
    public ResponseEntity<RouteDto> updateRouteStep(
            @PathVariable String id,
            @RequestParam String step) {
        
        log.info("Received request to update route step with ID: {} to step: {}", id, step);
        RouteDto updatedRoute = routingService.updateRouteStep(id, step);
        return ResponseEntity.ok(updatedRoute);
    }
    
    /**
     * Get equipment load status.
     * 
     * @return Map of equipment to load
     */
    @GetMapping("/equipment/load")
    public ResponseEntity<Map<String, Integer>> getEquipmentLoadStatus() {
        log.info("Received request to get equipment load status");
        Map<String, Integer> loadStatus = routingService.getEquipmentLoadStatus();
        return ResponseEntity.ok(loadStatus);
    }
    
    /**
     * Get equipment operational status.
     * 
     * @return Map of equipment to operational status
     */
    @GetMapping("/equipment/status")
    public ResponseEntity<Map<String, Boolean>> getEquipmentOperationalStatus() {
        log.info("Received request to get equipment operational status");
        Map<String, Boolean> operationalStatus = routingService.getEquipmentOperationalStatus();
        return ResponseEntity.ok(operationalStatus);
    }
    
    /**
     * Update equipment operational status.
     * 
     * @param equipment The equipment name
     * @param isOperational Whether the equipment is operational
     * @return Success message
     */
    @PutMapping("/equipment/{equipment}")
    public ResponseEntity<String> updateEquipmentStatus(
            @PathVariable String equipment,
            @RequestParam boolean isOperational) {
        
        log.info("Received request to update equipment status for {} to: {}", equipment, isOperational);
        routingService.updateEquipmentStatus(equipment, isOperational);
        return ResponseEntity.ok("Equipment status updated successfully");
    }
    
    /**
     * Get all available facilities.
     * 
     * @return List of facility names
     */
    @GetMapping("/facilities")
    public ResponseEntity<List<String>> getAllFacilities() {
        log.info("Received request to get all facilities");
        List<String> facilities = routingService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }
    
    /**
     * Get delayed routes (completion time exceeded estimated time).
     * 
     * @return List of delayed routes
     */
    @GetMapping("/delayed")
    public ResponseEntity<List<RouteDto>> getDelayedRoutes() {
        log.info("Received request to get delayed routes");
        List<RouteDto> delayedRoutes = routingService.getDelayedRoutes();
        return ResponseEntity.ok(delayedRoutes);
    }
    
    /**
     * Get average estimated time by step.
     * 
     * @return Map of step to average time
     */
    @GetMapping("/stats/time")
    public ResponseEntity<Map<String, Double>> getAverageTimeByStep() {
        log.info("Received request to get average time by step");
        Map<String, Double> averageTimes = routingService.getAverageTimeByStep();
        return ResponseEntity.ok(averageTimes);
    }
}
