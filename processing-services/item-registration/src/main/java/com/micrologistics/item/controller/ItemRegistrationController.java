package com.micrologistics.item.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.item.service.ItemRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for item registration and management.
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemRegistrationController {
    
    private final ItemRegistrationService itemService;
    
    /**
     * Register a new item.
     * 
     * @param itemDto The item data
     * @return The registered item
     */
    @PostMapping
    public ResponseEntity<ItemDto> registerItem(@Valid @RequestBody ItemDto itemDto) {
        log.info("Received request to register item: {}", itemDto.getDescription());
        ItemDto createdItem = itemService.registerItem(itemDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }
    
    /**
     * Get an item by its ID.
     * 
     * @param id The item ID
     * @return The item
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
        log.info("Received request to get item by ID: {}", id);
        ItemDto item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }
    
    /**
     * Get an item by its tracking ID.
     * 
     * @param trackingId The tracking ID
     * @return The item
     */
    @GetMapping("/tracking/{trackingId}")
    public ResponseEntity<ItemDto> getItemByTrackingId(@PathVariable String trackingId) {
        log.info("Received request to get item by tracking ID: {}", trackingId);
        ItemDto item = itemService.getItemByTrackingId(trackingId);
        return ResponseEntity.ok(item);
    }
    
    /**
     * Get all items, with optional pagination and filtering.
     * 
     * @param page The page number (0-indexed)
     * @param size The page size
     * @param status Optional status filter
     * @param sortBy Property to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return A page of items
     */
    @GetMapping
    public ResponseEntity<Page<ItemDto>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "registeredAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("Received request to get items, page: {}, size: {}, status: {}", page, size, status);
        
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ItemDto> items;
        if (status != null && !status.isEmpty()) {
            items = itemService.getItemsByStatus(status, pageable);
        } else {
            items = itemService.getAllItems(pageable);
        }
        
        return ResponseEntity.ok(items);
    }
    
    /**
     * Get items by destination.
     * 
     * @param destination The destination
     * @return A list of items
     */
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<ItemDto>> getItemsByDestination(@PathVariable String destination) {
        log.info("Received request to get items by destination: {}", destination);
        List<ItemDto> items = itemService.getItemsByDestination(destination);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Update an item.
     * 
     * @param id The item ID
     * @param itemDto The updated item data
     * @return The updated item
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(@PathVariable String id, @Valid @RequestBody ItemDto itemDto) {
        log.info("Received request to update item with ID: {}", id);
        ItemDto updatedItem = itemService.updateItem(id, itemDto);
        return ResponseEntity.ok(updatedItem);
    }
    
    /**
     * Update an item's status.
     * 
     * @param id The item ID
     * @param status The new status
     * @return The updated item
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ItemDto> updateItemStatus(@PathVariable String id, @RequestParam String status) {
        log.info("Received request to update item status with ID: {} to status: {}", id, status);
        ItemDto updatedItem = itemService.updateItemStatus(id, status);
        return ResponseEntity.ok(updatedItem);
    }
    
    /**
     * Delete an item.
     * 
     * @param id The item ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        log.info("Received request to delete item with ID: {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get item counts by status.
     * 
     * @return A map of status to count
     */
    @GetMapping("/stats/counts")
    public ResponseEntity<Map<String, Long>> getItemCountsByStatus() {
        log.info("Received request to get item counts by status");
        Map<String, Long> counts = itemService.getItemCountsByStatus();
        return ResponseEntity.ok(counts);
    }
    
    /**
     * Get average weight by destination.
     * 
     * @return A map of destination to average weight
     */
    @GetMapping("/stats/weight")
    public ResponseEntity<Map<String, Double>> getAverageWeightByDestination() {
        log.info("Received request to get average weight by destination");
        Map<String, Double> averageWeights = itemService.getAverageWeightByDestination();
        return ResponseEntity.ok(averageWeights);
    }
}
