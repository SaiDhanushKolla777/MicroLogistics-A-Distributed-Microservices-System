package com.micrologistics.item.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.item.entity.Item;
import com.micrologistics.item.mapper.ItemMapper;
import com.micrologistics.item.messaging.publisher.ItemEventPublisher;
import com.micrologistics.item.repository.ItemRepository;
import com.micrologistics.item.service.ItemRegistrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the ItemRegistrationService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRegistrationServiceImpl implements ItemRegistrationService {
    
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ItemDto registerItem(ItemDto itemDto) {
        log.info("Registering new item: {}", itemDto.getDescription());
        
        // Generate tracking ID if not provided
        if (itemDto.getTrackingId() == null || itemDto.getTrackingId().trim().isEmpty()) {
            itemDto.setTrackingId(generateTrackingId());
        } else if (itemRepository.existsByTrackingId(itemDto.getTrackingId())) {
            throw new BusinessException("Item with tracking ID already exists: " + itemDto.getTrackingId(),
                    BusinessException.ERROR_ITEM_ALREADY_EXISTS);
        }
        
        // Set default values
        itemDto.setStatus(Item.STATUS_REGISTERED);
        if (itemDto.getPriority() == null) {
            itemDto.setPriority(1); // Default priority
        }
        
        LocalDateTime now = LocalDateTime.now();
        itemDto.setRegisteredAt(now);
        itemDto.setUpdatedAt(now);
        
        // Convert to entity and save
        Item item = itemMapper.toEntity(itemDto);
        Item savedItem = itemRepository.save(item);
        log.info("Item registered successfully with ID: {}", savedItem.getId());
        
        // Publish event
        eventPublisher.publishItemRegisteredEvent(savedItem);
        
        return itemMapper.toDto(savedItem);
    }
    
    @Override
    public ItemDto getItemById(String id) {
        log.debug("Getting item by ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return itemMapper.toDto(item);
    }
    
    @Override
    public ItemDto getItemByTrackingId(String trackingId) {
        log.debug("Getting item by tracking ID: {}", trackingId);
        Item item = itemRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "trackingId", trackingId));
        return itemMapper.toDto(item);
    }
    
    @Override
    public Page<ItemDto> getAllItems(Pageable pageable) {
        log.debug("Getting all items with pagination: {}", pageable);
        Page<Item> itemsPage = itemRepository.findAll(pageable);
        return itemsPage.map(itemMapper::toDto);
    }
    
    @Override
    public Page<ItemDto> getItemsByStatus(String status, Pageable pageable) {
        log.debug("Getting items by status: {} with pagination: {}", status, pageable);
        Page<Item> itemsPage = itemRepository.findByStatus(status, pageable);
        return itemsPage.map(itemMapper::toDto);
    }
    
    @Override
    public List<ItemDto> getItemsByDestination(String destination) {
        log.debug("Getting items by destination: {}", destination);
        List<Item> items = itemRepository.findByDestination(destination);
        return itemMapper.toDtoList(items);
    }
    
    @Override
    @Transactional
    public ItemDto updateItem(String id, ItemDto itemDto) {
        log.info("Updating item with ID: {}", id);
        
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        
        // Update fields except ID, trackingId, registeredAt
        existingItem.setDescription(itemDto.getDescription());
        existingItem.setWeight(itemDto.getWeight());
        existingItem.setLength(itemDto.getLength());
        existingItem.setWidth(itemDto.getWidth());
        existingItem.setHeight(itemDto.getHeight());
        existingItem.setDestination(itemDto.getDestination());
        existingItem.setPriority(itemDto.getPriority());
        existingItem.setUpdatedAt(LocalDateTime.now());
        
        // Don't update status through this method
        
        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item updated successfully: {}", updatedItem.getId());
        
        return itemMapper.toDto(updatedItem);
    }
    
    @Override
    @Transactional
    public ItemDto updateItemStatus(String id, String status) {
        log.info("Updating item status with ID: {} to status: {}", id, status);
        
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        
        // Validate status transition
        validateStatusTransition(existingItem.getStatus(), status);
        
        existingItem.setStatus(status);
        existingItem.setUpdatedAt(LocalDateTime.now());
        
        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item status updated successfully: {}", updatedItem.getId());
        
        return itemMapper.toDto(updatedItem);
    }
    
    @Override
    @Transactional
    public void deleteItem(String id) {
        log.info("Deleting item with ID: {}", id);
        
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }
        
        itemRepository.deleteById(id);
        log.info("Item deleted successfully: {}", id);
    }
    
    @Override
    public Map<String, Long> getItemCountsByStatus() {
        log.debug("Getting item counts by status");
        
        Map<String, Long> counts = new HashMap<>();
        counts.put(Item.STATUS_REGISTERED, itemRepository.countByStatus(Item.STATUS_REGISTERED));
        counts.put(Item.STATUS_ROUTING, itemRepository.countByStatus(Item.STATUS_ROUTING));
        counts.put(Item.STATUS_PROCESSING, itemRepository.countByStatus(Item.STATUS_PROCESSING));
        counts.put(Item.STATUS_CONTAINERIZED, itemRepository.countByStatus(Item.STATUS_CONTAINERIZED));
        counts.put(Item.STATUS_SHIPPED, itemRepository.countByStatus(Item.STATUS_SHIPPED));
        
        return counts;
    }
    
    @Override
    public Map<String, Double> getAverageWeightByDestination() {
        log.debug("Getting average weight by destination");
        
        List<Object[]> results = itemRepository.findAverageWeightByDestination();
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Double) row[1]
                ));
    }
    
    /**
     * Generate a unique tracking ID.
     * 
     * @return A new tracking ID
     */
    private String generateTrackingId() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Validate a status transition.
     * 
     * @param currentStatus The current status
     * @param newStatus The new status
     * @throws BusinessException If the transition is invalid
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid transitions
        boolean isValid = switch (currentStatus) {
            case Item.STATUS_REGISTERED -> 
                newStatus.equals(Item.STATUS_ROUTING);
            case Item.STATUS_ROUTING -> 
                newStatus.equals(Item.STATUS_PROCESSING);
            case Item.STATUS_PROCESSING -> 
                newStatus.equals(Item.STATUS_CONTAINERIZED);
            case Item.STATUS_CONTAINERIZED -> 
                newStatus.equals(Item.STATUS_SHIPPED);
            case Item.STATUS_SHIPPED -> 
                false; // Terminal state, no valid transitions
            default -> false;
        };
        
        if (!isValid) {
            throw new BusinessException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus,
                    BusinessException.ERROR_INVALID_STATUS_TRANSITION);
        }
    }
}
