package com.micrologistics.container.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for containers in the logistics system.
 * Represents the persistent data model for shipping containers.
 */
@Entity
@Table(name = "containers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Container {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String containerNumber;
    
    @Column(nullable = false)
    private String destination;
    
    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;
    
    @Column(name = "max_volume", nullable = false)
    private Double maxVolume;
    
    @Column(name = "current_weight")
    private Double currentWeight;
    
    @Column(name = "current_volume")
    private Double currentVolume;
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(nullable = false)
    private String status;
    
    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ContainerItem> items = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    /**
     * Container status constants
     */
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_LOADING = "LOADING";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_DISPATCHED = "DISPATCHED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    
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
    
    /**
     * Add an item to the container.
     * 
     * @param item The item to add
     */
    public void addItem(ContainerItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        
        items.add(item);
        item.setContainer(this);
        
        // Update container metrics
        if (currentWeight == null) {
            currentWeight = 0.0;
        }
        currentWeight += item.getWeight();
        
        if (currentVolume == null) {
            currentVolume = 0.0;
        }
        currentVolume += item.getVolume();
        
        if (itemCount == null) {
            itemCount = 0;
        }
        itemCount += 1;
    }
    
    /**
     * Remove an item from the container.
     * 
     * @param item The item to remove
     */
    public void removeItem(ContainerItem item) {
        if (items != null && items.remove(item)) {
            item.setContainer(null);
            
            // Update container metrics
            currentWeight -= item.getWeight();
            currentVolume -= item.getVolume();
            itemCount -= 1;
        }
    }
    
    /**
     * Get a list of item IDs in the container.
     * 
     * @return List of item IDs
     */
    public List<String> getItemIds() {
        List<String> itemIds = new ArrayList<>();
        if (items != null) {
            for (ContainerItem item : items) {
                itemIds.add(item.getItemId());
            }
        }
        return itemIds;
    }
    
    /**
     * Check if the container is empty.
     * 
     * @return True if the container has no items
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    /**
     * Check if the container can be closed.
     * 
     * @return True if the container is in LOADING status and has items
     */
    public boolean canBeClosed() {
        return STATUS_LOADING.equals(status) && !isEmpty();
    }
    
    /**
     * Check if the container can be dispatched.
     * 
     * @return True if the container is in CLOSED status
     */
    public boolean canBeDispatched() {
        return STATUS_CLOSED.equals(status);
    }
    
    /**
     * Initialize the container values for a new container.
     */
    public void initialize() {
        this.currentWeight = 0.0;
        this.currentVolume = 0.0;
        this.itemCount = 0;
        this.status = STATUS_CREATED;
        this.createdAt = LocalDateTime.now();
    }
}
