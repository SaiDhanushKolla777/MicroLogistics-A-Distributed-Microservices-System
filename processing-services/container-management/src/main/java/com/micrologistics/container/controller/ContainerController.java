package com.micrologistics.container.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.micrologistics.common.dto.ContainerDto;
import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.container.service.ContainerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for container management.
 */
@RestController
@RequestMapping("/api/containers")
@RequiredArgsConstructor
@Slf4j
public class ContainerController {
    
    private final ContainerService containerService;
    
    /**
     * Create a new container.
     * 
     * @param containerDto The container data
     * @return The created container
     */
    @PostMapping
    public ResponseEntity<ContainerDto> createContainer(@Valid @RequestBody ContainerDto containerDto) {
        log.info("Received request to create container: {}", containerDto.getDestination());
        ContainerDto createdContainer = containerService.createContainer(containerDto);
        return new ResponseEntity<>(createdContainer, HttpStatus.CREATED);
    }
    
    /**
     * Get a container by its ID.
     * 
     * @param id The container ID
     * @return The container
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContainerDto> getContainerById(@PathVariable String id) {
        log.info("Received request to get container by ID: {}", id);
        ContainerDto container = containerService.getContainerById(id);
        return ResponseEntity.ok(container);
    }
    
    /**
     * Get a container by its container number.
     * 
     * @param containerNumber The container number
     * @return The container
     */
    @GetMapping("/number/{containerNumber}")
    public ResponseEntity<ContainerDto> getContainerByNumber(@PathVariable String containerNumber) {
        log.info("Received request to get container by number: {}", containerNumber);
        ContainerDto container = containerService.getContainerByNumber(containerNumber);
        return ResponseEntity.ok(container);
    }
    
    /**
     * Get all containers, with optional pagination and filtering.
     * 
     * @param page The page number (0-indexed)
     * @param size The page size
     * @param status Optional status filter
     * @param destination Optional destination filter
     * @param sortBy Property to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return A page of containers
     */
    @GetMapping
    public ResponseEntity<Page<ContainerDto>> getContainers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("Received request to get containers, page: {}, size: {}, status: {}, destination: {}", 
                page, size, status, destination);
        
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ContainerDto> containers;
        if (status != null && destination != null) {
            containers = containerService.getContainersByStatusAndDestination(status, destination, pageable);
        } else if (status != null) {
            containers = containerService.getContainersByStatus(status, pageable);
        } else if (destination != null) {
            containers = containerService.getContainersByDestination(destination, pageable);
        } else {
            containers = containerService.getAllContainers(pageable);
        }
        
        return ResponseEntity.ok(containers);
    }
    
    /**
     * Get containers containing a specific item.
     * 
     * @param itemId The item ID
     * @return A list of containers containing the specified item
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ContainerDto>> getContainersContainingItem(@PathVariable String itemId) {
        log.info("Received request to get containers containing item: {}", itemId);
        List<ContainerDto> containers = containerService.getContainersContainingItem(itemId);
        return ResponseEntity.ok(containers);
    }
    
    /**
     * Get available containers for a destination.
     * 
     * @param destination The destination
     * @return A list of available containers
     */
    @GetMapping("/available")
    public ResponseEntity<List<ContainerDto>> getAvailableContainersForDestination(
            @RequestParam String destination) {
        log.info("Received request to get available containers for destination: {}", destination);
        List<ContainerDto> containers = containerService.getAvailableContainersForDestination(destination);
        return ResponseEntity.ok(containers);
    }
    
    /**
     * Add an item to a container.
     * 
     * @param containerId The container ID
     * @param itemDto The item data
     * @return The updated container
     */
    @PostMapping("/{containerId}/items")
    public ResponseEntity<ContainerDto> addItemToContainer(
            @PathVariable String containerId,
            @Valid @RequestBody ItemDto itemDto) {
        log.info("Received request to add item to container: {}", containerId);
        ContainerDto updatedContainer = containerService.addItemToContainer(containerId, itemDto);
        return ResponseEntity.ok(updatedContainer);
    }
    
    /**
     * Remove an item from a container.
     * 
     * @param containerId The container ID
     * @param itemId The item ID
     * @return The updated container
     */
    @DeleteMapping("/{containerId}/items/{itemId}")
    public ResponseEntity<ContainerDto> removeItemFromContainer(
            @PathVariable String containerId,
            @PathVariable String itemId) {
        log.info("Received request to remove item {} from container: {}", itemId, containerId);
        ContainerDto updatedContainer = containerService.removeItemFromContainer(containerId, itemId);
        return ResponseEntity.ok(updatedContainer);
    }
    
    /**
     * Get all items in a container.
     * 
     * @param containerId The container ID
     * @return A list of items in the container
     */
    @GetMapping("/{containerId}/items")
    public ResponseEntity<List<ItemDto>> getItemsInContainer(@PathVariable String containerId) {
        log.info("Received request to get items in container: {}", containerId);
        List<ItemDto> items = containerService.getItemsInContainer(containerId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Update a container's status.
     * 
     * @param id The container ID
     * @param status The new status
     * @return The updated container
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ContainerDto> updateContainerStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("Received request to update container status: {} to {}", id, status);
        ContainerDto updatedContainer = containerService.updateContainerStatus(id, status);
        return ResponseEntity.ok(updatedContainer);
    }
    
    /**
     * Close a container.
     * 
     * @param id The container ID
     * @return The closed container
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ContainerDto> closeContainer(@PathVariable String id) {
        log.info("Received request to close container: {}", id);
        ContainerDto closedContainer = containerService.closeContainer(id);
        return ResponseEntity.ok(closedContainer);
    }
    
    /**
     * Dispatch a container.
     * 
     * @param id The container ID
     * @return The dispatched container
     */
    @PutMapping("/{id}/dispatch")
    public ResponseEntity<ContainerDto> dispatchContainer(@PathVariable String id) {
        log.info("Received request to dispatch container: {}", id);
        ContainerDto dispatchedContainer = containerService.dispatchContainer(id);
        return ResponseEntity.ok(dispatchedContainer);
    }
    
    /**
     * Mark a container as delivered.
     * 
     * @param id The container ID
     * @return The delivered container
     */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<ContainerDto> markContainerDelivered(@PathVariable String id) {
        log.info("Received request to mark container as delivered: {}", id);
        ContainerDto deliveredContainer = containerService.markContainerDelivered(id);
        return ResponseEntity.ok(deliveredContainer);
    }
    
    /**
     * Delete a container.
     * 
     * @param id The container ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable String id) {
        log.info("Received request to delete container: {}", id);
        containerService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Find the optimal container for an item.
     * 
     * @param itemDto The item data
     * @return The optimal container, or 404 if no suitable container is found
     */
    @PostMapping("/optimal")
    public ResponseEntity<ContainerDto> findOptimalContainerForItem(@RequestBody ItemDto itemDto) {
        log.info("Received request to find optimal container for item: {}", itemDto.getId());
        ContainerDto optimalContainer = containerService.findOptimalContainerForItem(itemDto);
        
        if (optimalContainer == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(optimalContainer);
    }
    
    /**
     * Get container counts by status.
     * 
     * @return A map of status to count
     */
    @GetMapping("/stats/counts")
    public ResponseEntity<Map<String, Long>> getContainerCountsByStatus() {
        log.info("Received request to get container counts by status");
        Map<String, Long> counts = containerService.getContainerCountsByStatus();
        return ResponseEntity.ok(counts);
    }
    
    /**
     * Get the average utilization by destination.
     * 
     * @return A map of destination to utilization data
     */
    @GetMapping("/stats/utilization")
    public ResponseEntity<Map<String, Map<String, Double>>> getAverageUtilizationByDestination() {
        log.info("Received request to get average utilization by destination");
        Map<String, Map<String, Double>> utilization = containerService.getAverageUtilizationByDestination();
        return ResponseEntity.ok(utilization);
    }
    
    /**
     * Get containers with high utilization.
     * 
     * @return A list of highly utilized containers
     */
    @GetMapping("/high-utilization")
    public ResponseEntity<List<ContainerDto>> getContainersWithHighUtilization() {
        log.info("Received request to get containers with high utilization");
        List<ContainerDto> containers = containerService.getContainersWithHighUtilization();
        return ResponseEntity.ok(containers);
    }
    
    /**
     * Get containers dispatched within a time range.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of containers dispatched within the specified time range
     */
    @GetMapping("/dispatched")
    public ResponseEntity<List<ContainerDto>> getContainersDispatchedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Received request to get containers dispatched between {} and {}", startTime, endTime);
        List<ContainerDto> containers = containerService.getContainersDispatchedBetween(startTime, endTime);
        return ResponseEntity.ok(containers);
    }
}
