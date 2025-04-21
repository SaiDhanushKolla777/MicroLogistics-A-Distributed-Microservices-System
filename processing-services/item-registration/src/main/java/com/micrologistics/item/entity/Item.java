package com.micrologistics.item.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for items in the logistics system.
 * Represents the persistent data model for items.
 */
@Entity
@Table(name = "items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
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
    
    @Column(nullable = false)
    private String destination;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private Integer priority;
    
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Status constants
     */
    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_ROUTING = "ROUTING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_CONTAINERIZED = "CONTAINERIZED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    
    /**
     * Calculate the volume of the item.
     * 
     * @return The volume in cubic units
     */
    public Double getVolume() {
        if (length != null && width != null && height != null) {
            return length * width * height;
        }
        return null;
    }
}
