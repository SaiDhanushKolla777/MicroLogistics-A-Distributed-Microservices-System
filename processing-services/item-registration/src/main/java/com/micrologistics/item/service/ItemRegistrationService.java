package com.micrologistics.item.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.micrologistics.common.dto.ItemDto;

/**
 * Service interface for item registration and management.
 */
public interface ItemRegistrationService {
    
    /**
     * Register a new item in the system.
     * 
     * @param itemDto The item data
     * @return The registered item
     */
    ItemDto registerItem(ItemDto itemDto);
    
    /**
     * Get an item by its ID.
     * 
     * @param id The item ID
     * @return The item
     */
    ItemDto getItemById(String id);
    
    /**
     * Get an item by its tracking ID.
     * 
     * @param trackingId The item tracking ID
     * @return The item
     */
    ItemDto getItemByTrackingId(String trackingId);
    
    /**
     * Get all items, with optional pagination.
     * 
     * @param pageable Pagination information
     * @return A page of items
     */
    Page<ItemDto> getAllItems(Pageable pageable);
    
    /**
     * Get items by status, with optional pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of items
     */
    Page<ItemDto> getItemsByStatus(String status, Pageable pageable);
    
    /**
     * Get items by destination.
     * 
     * @param destination The destination to filter by
     * @return A list of items
     */
    List<ItemDto> getItemsByDestination(String destination);
    
    /**
     * Update an item's information.
     * 
     * @param id The item ID
     * @param itemDto The updated item data
     * @return The updated item
     */
    ItemDto updateItem(String id, ItemDto itemDto);
    
    /**
     * Update an item's status.
     * 
     * @param id The item ID
     * @param status The new status
     * @return The updated item
     */
    ItemDto updateItemStatus(String id, String status);
    
    /**
     * Delete an item by its ID.
     * 
     * @param id The item ID
     */
    void deleteItem(String id);
    
    /**
     * Get item counts by status.
     * 
     * @return A map of status to count
     */
    Map<String, Long> getItemCountsByStatus();
    
    /**
     * Get average weight by destination.
     * 
     * @return A map of destination to average weight
     */
    Map<String, Double> getAverageWeightByDestination();
}
