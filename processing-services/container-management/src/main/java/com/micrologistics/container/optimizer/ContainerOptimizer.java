package com.micrologistics.container.optimizer;

import java.util.List;

import org.springframework.stereotype.Component;

import com.micrologistics.container.entity.Container;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Optimizer for finding the best container for an item.
 * Uses packing algorithms to optimize space utilization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerOptimizer {
    
    private final PackingAlgorithm packingAlgorithm;
    
    /**
     * Find the optimal container for an item with the given weight and volume.
     * 
     * @param availableContainers List of available containers
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The optimal container, or null if no suitable container is found
     */
    public Container findOptimalContainer(List<Container> availableContainers, double itemWeight, double itemVolume) {
        log.debug("Finding optimal container for item with weight={}, volume={}", itemWeight, itemVolume);
        
        if (availableContainers == null || availableContainers.isEmpty()) {
            log.debug("No available containers provided");
            return null;
        }
        
        // First filter containers that have capacity for the item
        List<Container> suitableContainers = availableContainers.stream()
                .filter(c -> c.hasCapacityFor(itemWeight, itemVolume))
                .toList();
        
        if (suitableContainers.isEmpty()) {
            log.debug("No containers with sufficient capacity found");
            return null;
        }
        
        // Use the packing algorithm to find the best container
        return packingAlgorithm.findBestContainer(suitableContainers, itemWeight, itemVolume);
    }
    
    /**
     * Check if an item can fit in a specific container.
     * 
     * @param container The container
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return True if the item can fit in the container
     */
    public boolean canFitInContainer(Container container, double itemWeight, double itemVolume) {
        if (container == null) {
            return false;
        }
        
        return container.hasCapacityFor(itemWeight, itemVolume);
    }
}
