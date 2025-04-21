package com.micrologistics.container.optimizer;

import java.util.List;

import org.springframework.stereotype.Component;

import com.micrologistics.container.entity.Container;

import lombok.extern.slf4j.Slf4j;

/**
 * Algorithm for optimizing container packing.
 * Implements different strategies for finding the best container for an item.
 */
@Component
@Slf4j
public class PackingAlgorithm {
    
    // Different packing strategies
    public enum Strategy {
        BEST_FIT,        // Choose container with least remaining space after packing
        FIRST_FIT,       // Choose first container that can fit the item
        WORST_FIT,       // Choose container with most remaining space after packing
        WEIGHT_BALANCED  // Balance weight across containers
    }
    
    // Default strategy
    private Strategy currentStrategy = Strategy.BEST_FIT;
    
    /**
     * Set the packing strategy to use.
     * 
     * @param strategy The strategy to use
     */
    public void setStrategy(Strategy strategy) {
        this.currentStrategy = strategy;
    }
    
    /**
     * Find the best container for an item using the current strategy.
     * 
     * @param containers List of containers to consider
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The best container, or null if no suitable container is found
     */
    public Container findBestContainer(List<Container> containers, double itemWeight, double itemVolume) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        
        switch (currentStrategy) {
            case BEST_FIT:
                return findBestFitContainer(containers, itemWeight, itemVolume);
            case FIRST_FIT:
                return findFirstFitContainer(containers, itemWeight, itemVolume);
            case WORST_FIT:
                return findWorstFitContainer(containers, itemWeight, itemVolume);
            case WEIGHT_BALANCED:
                return findWeightBalancedContainer(containers, itemWeight, itemVolume);
            default:
                return findBestFitContainer(containers, itemWeight, itemVolume);
        }
    }
    
    /**
     * Find the container with the least remaining space after packing.
     * 
     * @param containers List of containers to consider
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The best-fit container, or null if no suitable container is found
     */
    private Container findBestFitContainer(List<Container> containers, double itemWeight, double itemVolume) {
        log.debug("Using BEST FIT strategy to find container");
        
        Container bestContainer = null;
        double bestRemainingVolume = Double.MAX_VALUE;
        
        for (Container container : containers) {
            double currentWeight = container.getCurrentWeight() != null ? container.getCurrentWeight() : 0.0;
            double currentVolume = container.getCurrentVolume() != null ? container.getCurrentVolume() : 0.0;
            
            if (currentWeight + itemWeight <= container.getMaxWeight() && 
                currentVolume + itemVolume <= container.getMaxVolume()) {
                
                double remainingVolume = container.getMaxVolume() - (currentVolume + itemVolume);
                
                if (remainingVolume < bestRemainingVolume) {
                    bestRemainingVolume = remainingVolume;
                    bestContainer = container;
                }
            }
        }
        
        return bestContainer;
    }
    
    /**
     * Find the first container that can fit the item.
     * 
     * @param containers List of containers to consider
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The first-fit container, or null if no suitable container is found
     */
    private Container findFirstFitContainer(List<Container> containers, double itemWeight, double itemVolume) {
        log.debug("Using FIRST FIT strategy to find container");
        
        for (Container container : containers) {
            double currentWeight = container.getCurrentWeight() != null ? container.getCurrentWeight() : 0.0;
            double currentVolume = container.getCurrentVolume() != null ? container.getCurrentVolume() : 0.0;
            
            if (currentWeight + itemWeight <= container.getMaxWeight() && 
                currentVolume + itemVolume <= container.getMaxVolume()) {
                
                return container;
            }
        }
        
        return null;
    }
    
    /**
     * Find the container with the most remaining space after packing.
     * 
     * @param containers List of containers to consider
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The worst-fit container, or null if no suitable container is found
     */
    private Container findWorstFitContainer(List<Container> containers, double itemWeight, double itemVolume) {
        log.debug("Using WORST FIT strategy to find container");
        
        Container worstContainer = null;
        double worstRemainingVolume = -1.0;
        
        for (Container container : containers) {
            double currentWeight = container.getCurrentWeight() != null ? container.getCurrentWeight() : 0.0;
            double currentVolume = container.getCurrentVolume() != null ? container.getCurrentVolume() : 0.0;
            
            if (currentWeight + itemWeight <= container.getMaxWeight() && 
                currentVolume + itemVolume <= container.getMaxVolume()) {
                
                double remainingVolume = container.getMaxVolume() - (currentVolume + itemVolume);
                
                if (remainingVolume > worstRemainingVolume) {
                    worstRemainingVolume = remainingVolume;
                    worstContainer = container;
                }
            }
        }
        
        return worstContainer;
    }
    
    /**
     * Find the container that would have the most balanced weight distribution.
     * 
     * @param containers List of containers to consider
     * @param itemWeight The weight of the item
     * @param itemVolume The volume of the item
     * @return The weight-balanced container, or null if no suitable container is found
     */
    private Container findWeightBalancedContainer(List<Container> containers, double itemWeight, double itemVolume) {
        log.debug("Using WEIGHT BALANCED strategy to find container");
        
        Container balancedContainer = null;
        double mostBalancedRatio = Double.MAX_VALUE;
        
        for (Container container : containers) {
            double currentWeight = container.getCurrentWeight() != null ? container.getCurrentWeight() : 0.0;
            double currentVolume = container.getCurrentVolume() != null ? container.getCurrentVolume() : 0.0;
            
            if (currentWeight + itemWeight <= container.getMaxWeight() && 
                currentVolume + itemVolume <= container.getMaxVolume()) {
                
                // Calculate how close to 50% weight capacity this would be (perfect balance)
                double newWeightRatio = (currentWeight + itemWeight) / container.getMaxWeight();
                double distanceFromIdeal = Math.abs(0.5 - newWeightRatio);
                
                if (distanceFromIdeal < mostBalancedRatio) {
                    mostBalancedRatio = distanceFromIdeal;
                    balancedContainer = container;
                }
            }
        }
        
        return balancedContainer;
    }
}
