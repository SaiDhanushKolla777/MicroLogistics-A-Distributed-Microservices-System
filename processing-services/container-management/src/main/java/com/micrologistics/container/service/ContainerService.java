package com.micrologistics.container.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.micrologistics.common.dto.ContainerDto;
import com.micrologistics.common.dto.ItemDto;

/**
 * Service interface for container management.
 */
public interface ContainerService {
    
    /**
     * Create a new container.
     * 
     * @param containerDto The container data
     * @return The created container
     */
    ContainerDto createContainer(ContainerDto containerDto);
    
    /**
     * Get a container by its ID.
     * 
     * @param id The container ID
     * @return The container
     */
    ContainerDto getContainerById(String id);
    
    /**
     * Get a container by its container number.
     * 
     * @param containerNumber The container number
     * @return The container
     */
    ContainerDto getContainerByNumber(String containerNumber);
    
    /**
     * Get all containers, with optional pagination.
     * 
     * @param pageable Pagination information
     * @return A page of containers
     */
    Page<ContainerDto> getAllContainers(Pageable pageable);
    
    /**
     * Get containers by status, with optional pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of containers
     */
    Page<ContainerDto> getContainersByStatus(String status, Pageable pageable);
    
    /**
     * Get containers by destination, with optional pagination.
     * 
     * @param destination The destination to filter by
     * @param pageable Pagination information
     * @return A page of containers
     */
    Page<ContainerDto> getContainersByDestination(String destination, Pageable pageable);
    
    /**
     * Get containers by status and destination, with optional pagination.
     * 
     * @param status The status to filter by
     * @param destination The destination to filter by
     * @param pageable Pagination information
     * @return A page of containers
     */
    Page<ContainerDto> getContainersByStatusAndDestination(String status, String destination, Pageable pageable);
    
    /**
     * Get containers that contain a specific item.
     * 
     * @param itemId The item ID to look for
     * @return A list of containers that contain the specified item
     */
    List<ContainerDto> getContainersContainingItem(String itemId);
    
    /**
     * Find available containers for loading items to a specific destination.
     * 
     * @param destination The destination
     * @return A list of containers that are available for loading
     */
    List<ContainerDto> getAvailableContainersForDestination(String destination);
    
    /**
     * Add an item to a container.
     * 
     * @param containerId The container ID
     * @param itemDto The item data
     * @return The updated container
     */
    ContainerDto addItemToContainer(String containerId, ItemDto itemDto);
    
    /**
     * Remove an item from a container.
     * 
     * @param containerId The container ID
     * @param itemId The item ID
     * @return The updated container
     */
    ContainerDto removeItemFromContainer(String containerId, String itemId);
    
    /**
     * Get all items in a container.
     * 
     * @param containerId The container ID
     * @return A list of items in the container
     */
    List<ItemDto> getItemsInContainer(String containerId);
    
    /**
     * Update a container's status.
     * 
     * @param id The container ID
     * @param status The new status
     * @return The updated container
     */
    ContainerDto updateContainerStatus(String id, String status);
    
    /**
     * Close a container, preventing further items from being added.
     * 
     * @param id The container ID
     * @return The closed container
     */
    ContainerDto closeContainer(String id);
    
    /**
     * Dispatch a container for delivery.
     * 
     * @param id The container ID
     * @return The dispatched container
     */
    ContainerDto dispatchContainer(String id);
    
    /**
     * Mark a container as delivered.
     * 
     * @param id The container ID
     * @return The delivered container
     */
    ContainerDto markContainerDelivered(String id);
    
    /**
     * Delete a container.
     * 
     * @param id The container ID
     */
    void deleteContainer(String id);
    
    /**
     * Find the optimal container for an item.
     * 
     * @param itemDto The item data
     * @return The optimal container, or null if no suitable container is found
     */
    ContainerDto findOptimalContainerForItem(ItemDto itemDto);
    
    /**
     * Get container counts by status.
     * 
     * @return A map of status to count
     */
    Map<String, Long> getContainerCountsByStatus();
    
    /**
     * Get the average utilization by destination.
     * 
     * @return A map of destination to utilization data
     */
    Map<String, Map<String, Double>> getAverageUtilizationByDestination();
    
    /**
     * Get containers with high utilization.
     * 
     * @return A list of highly utilized containers
     */
    List<ContainerDto> getContainersWithHighUtilization();
    
    /**
     * Get containers dispatched within a time range.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of containers dispatched within the specified time range
     */
    List<ContainerDto> getContainersDispatchedBetween(LocalDateTime startTime, LocalDateTime endTime);
}
