package com.micrologistics.routing.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Algorithm to find the optimal routing path for items based on various factors
 * including destination, current equipment load, and item properties.
 */
@Component
@Slf4j
public class OptimalPathFinder {
    
    // Simulated equipment load (would come from a real-time monitoring system)
    private final Map<String, Integer> equipmentLoad = new ConcurrentHashMap<>();
    
    // Simulated processing facilities
    private final List<String> facilities = Arrays.asList(
        "INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A", "SORTING_AREA_B", 
        "PACKAGING_AREA", "CONTAINER_LOADING", "OUTBOUND_DOCK_NORTH", "OUTBOUND_DOCK_SOUTH",
        "OUTBOUND_DOCK_EAST", "OUTBOUND_DOCK_WEST"
    );
    
    // Average processing time per facility in minutes
    private final Map<String, Double> processingTimes = new HashMap<>();
    
    // Mapping of destinations to optimal outbound docks
    private final Map<String, String> destinationToOutboundDock = new HashMap<>();
    
    // Equipment operational status
    private final Map<String, Boolean> equipmentOperational = new ConcurrentHashMap<>();
    
    /**
     * Constructor to initialize the path finder.
     */
    public OptimalPathFinder() {
        // Initialize with random equipment loads
        Random random = new Random();
        for (String facility : facilities) {
            equipmentLoad.put(facility, random.nextInt(100));
            equipmentOperational.put(facility, true);
        }
        
        // Initialize processing times
        processingTimes.put("INBOUND_DOCK", 5.0);
        processingTimes.put("SCANNER_STATION", 2.0);
        processingTimes.put("SORTING_AREA_A", 8.0);
        processingTimes.put("SORTING_AREA_B", 10.0);
        processingTimes.put("PACKAGING_AREA", 15.0);
        processingTimes.put("CONTAINER_LOADING", 12.0);
        processingTimes.put("OUTBOUND_DOCK_NORTH", 7.0);
        processingTimes.put("OUTBOUND_DOCK_SOUTH", 7.0);
        processingTimes.put("OUTBOUND_DOCK_EAST", 7.0);
        processingTimes.put("OUTBOUND_DOCK_WEST", 7.0);
        
        // Initialize destination mappings (simplified for demonstration)
        destinationToOutboundDock.put("NORTH", "OUTBOUND_DOCK_NORTH");
        destinationToOutboundDock.put("SOUTH", "OUTBOUND_DOCK_SOUTH");
        destinationToOutboundDock.put("EAST", "OUTBOUND_DOCK_EAST");
        destinationToOutboundDock.put("WEST", "OUTBOUND_DOCK_WEST");
        
        // Default mappings for common cities/regions
        destinationToOutboundDock.put("NEW YORK", "OUTBOUND_DOCK_EAST");
        destinationToOutboundDock.put("LOS ANGELES", "OUTBOUND_DOCK_WEST");
        destinationToOutboundDock.put("CHICAGO", "OUTBOUND_DOCK_NORTH");
        destinationToOutboundDock.put("HOUSTON", "OUTBOUND_DOCK_SOUTH");
        destinationToOutboundDock.put("MIAMI", "OUTBOUND_DOCK_SOUTH");
        destinationToOutboundDock.put("SEATTLE", "OUTBOUND_DOCK_WEST");
        destinationToOutboundDock.put("BOSTON", "OUTBOUND_DOCK_EAST");
    }
    
    /**
     * Calculates the optimal path for an item based on its properties and current system state.
     * 
     * @param itemId The ID of the item
     * @param destination The destination of the item
     * @param weight The weight of the item
     * @param priority Priority level (higher means more urgent)
     * @return List of processing steps the item should follow
     */
    public List<String> findOptimalPath(String itemId, String destination, double weight, Integer priority) {
        log.info("Finding optimal path for item {}, destination: {}, weight: {}, priority: {}", 
                itemId, destination, weight, priority);
        
        List<String> path = new ArrayList<>();
        
        // Start with inbound dock
        path.add("INBOUND_DOCK");
        
        // All items go through scanner
        path.add("SCANNER_STATION");
        
        // Choose sorting area based on load balancing and weight
        String sortingArea = chooseSortingArea(weight);
        path.add(sortingArea);
        
        // Add packaging area for all items
        path.add("PACKAGING_AREA");
        
        // Add container loading
        path.add("CONTAINER_LOADING");
        
        // Add appropriate outbound dock based on destination
        String region = extractRegion(destination);
        String outboundDock = destinationToOutboundDock.getOrDefault(region, "OUTBOUND_DOCK_SOUTH");
        path.add(outboundDock);
        
        // Update simulated equipment load
        updateEquipmentLoads(path);
        
        log.info("Determined path for item {}: {}", itemId, path);
        return path;
    }
    
    /**
     * Calculate the total estimated processing time for a route.
     * 
     * @param path The path of processing steps
     * @return The estimated time in minutes
     */
    public double calculateEstimatedTime(List<String> path) {
        // Base processing time based on each facility
        double totalTime = path.stream()
                .mapToDouble(step -> processingTimes.getOrDefault(step, 5.0))
                .sum();
        
        // Add time based on current equipment loads (busier = slower)
        for (String step : path) {
            int load = equipmentLoad.getOrDefault(step, 0);
            // Add 0-100% extra time based on load (0-100)
            totalTime += processingTimes.getOrDefault(step, 5.0) * (load / 100.0);
        }
        
        return totalTime;
    }
    
    /**
     * Choose the sorting area based on weight and current load.
     * 
     * @param weight The item weight
     * @return The selected sorting area
     */
    private String chooseSortingArea(double weight) {
        // Check if either sorting area is non-operational
        if (!equipmentOperational.getOrDefault("SORTING_AREA_A", true)) {
            return "SORTING_AREA_B";
        }
        
        if (!equipmentOperational.getOrDefault("SORTING_AREA_B", true)) {
            return "SORTING_AREA_A";
        }
        
        int loadA = equipmentLoad.getOrDefault("SORTING_AREA_A", 0);
        int loadB = equipmentLoad.getOrDefault("SORTING_AREA_B", 0);
        
        // Heavy items go to sorting area B unless it's significantly more loaded
        if (weight > 20.0) {
            return (loadB > loadA + 30) ? "SORTING_AREA_A" : "SORTING_AREA_B";
        } else {
            return (loadA > loadB + 30) ? "SORTING_AREA_B" : "SORTING_AREA_A";
        }
    }
    
    /**
     * Extract region from destination string.
     * 
     * @param destination The destination
     * @return The region
     */
    private String extractRegion(String destination) {
        if (destination == null) {
            return "SOUTH"; // Default region
        }
        
        String upperDestination = destination.toUpperCase();
        
        // First check if we have an exact match in our mapping
        if (destinationToOutboundDock.containsKey(upperDestination)) {
            return upperDestination;
        }
        
        // Then check for region keywords
        if (upperDestination.contains("NORTH")) return "NORTH";
        if (upperDestination.contains("SOUTH")) return "SOUTH";
        if (upperDestination.contains("EAST")) return "EAST";
        if (upperDestination.contains("WEST")) return "WEST";
        
        // If no match, use the default region
        return "SOUTH";
    }
    
    /**
     * Update equipment loads based on a new path.
     * 
     * @param path The path to update loads for
     */
    private void updateEquipmentLoads(List<String> path) {
        for (String step : path) {
            int currentLoad = equipmentLoad.getOrDefault(step, 0);
            // Increment load by a small amount (1-3 units)
            int increment = new Random().nextInt(3) + 1;
            equipmentLoad.put(step, Math.min(currentLoad + increment, 100));
        }
        
        // Simulate some load reduction on random facilities (natural completion of work)
        for (int i = 0; i < 2; i++) {
            String randomFacility = facilities.get(new Random().nextInt(facilities.size()));
            int currentLoad = equipmentLoad.getOrDefault(randomFacility, 0);
            if (currentLoad > 5) {
                // Reduce by 1-5 units
                int reduction = new Random().nextInt(5) + 1;
                equipmentLoad.put(randomFacility, Math.max(currentLoad - reduction, 0));
            }
        }
    }
    
    /**
     * Updates the status of equipment.
     * 
     * @param equipment The equipment name
     * @param isOperational Whether the equipment is operational
     */
    public void updateEquipmentStatus(String equipment, boolean isOperational) {
        log.info("Updating equipment status for {}: operational = {}", equipment, isOperational);
        
        equipmentOperational.put(equipment, isOperational);
        
        // If equipment is not operational, set its load to maximum to prevent routing
        if (!isOperational) {
            equipmentLoad.put(equipment, 100);
            log.warn("Equipment {} is now non-operational", equipment);
        } else {
            // Reset to a moderate load when equipment becomes operational again
            equipmentLoad.put(equipment, 20);
            log.info("Equipment {} is now operational", equipment);
        }
    }
    
    /**
     * Get the current load status of all equipment.
     * 
     * @return A map of equipment to load
     */
    public Map<String, Integer> getEquipmentLoadStatus() {
        return new HashMap<>(equipmentLoad);
    }
    
    /**
     * Get the operational status of all equipment.
     * 
     * @return A map of equipment to operational status
     */
    public Map<String, Boolean> getEquipmentOperationalStatus() {
        return new HashMap<>(equipmentOperational);
    }
    
    /**
     * Get all processing facilities.
     * 
     * @return List of facilities
     */
    public List<String> getAllFacilities() {
        return new ArrayList<>(facilities);
    }
}
