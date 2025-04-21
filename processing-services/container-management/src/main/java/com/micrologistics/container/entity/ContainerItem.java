package com.micrologistics.container.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity class for items stored in a container.
 * Represents the relationship between items and containers.
 */
@Entity
@Table(name = "container_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "item_id", nullable = false)
    private String itemId;
    
    @Column(name = "tracking_id", nullable = false)
    private String trackingId;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Double weight;
    
    @Column(nullable = false)
    private Double length;
    
    @Column(nullable = false)
    private Double width;
    
    @Column(nullable = false)
    private Double height;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Container container;
    
    /**
     * Calculate the volume of the item.
     * 
     * @return The volume in cubic units
     */
    public Double getVolume() {
        if (length != null && width != null && height != null) {
            return length * width * height;
        }
        return 0.0;
    }
    
    /**
     * Check if this item is assigned to a container.
     * 
     * @return True if the item is assigned to a container
     */
    public boolean isAssigned() {
        return container != null;
    }
}
