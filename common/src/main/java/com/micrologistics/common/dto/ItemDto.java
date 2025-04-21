package com.micrologistics.common.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for items in the logistics system.
 * Used to transfer item data between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    
    private String id;
    
    private String trackingId;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be less than 256 characters")
    private String description;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    @NotNull(message = "Length is required")
    @Positive(message = "Length must be positive")
    private Double length;
    
    @NotNull(message = "Width is required")
    @Positive(message = "Width must be positive")
    private Double width;
    
    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    private Double height;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    private String status;
    
    private Integer priority;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registeredAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
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
