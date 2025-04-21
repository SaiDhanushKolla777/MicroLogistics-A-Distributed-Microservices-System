package com.micrologistics.container.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micrologistics.common.dto.ContainerDto;
import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.event.ContainerStatusEvent;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.container.entity.Container;
import com.micrologistics.container.entity.ContainerItem;
import com.micrologistics.container.mapper.ContainerMapper;
import com.micrologistics.container.mapper.ItemMapper;
import com.micrologistics.container.messaging.publisher.ContainerEventPublisher;
import com.micrologistics.container.optimizer.ContainerOptimizer;
import com.micrologistics.container.repository.ContainerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the ContainerService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerServiceImpl implements com.micrologistics.container.service.ContainerService {
    
    private final ContainerRepository containerRepository;
    private final ContainerMapper containerMapper;
    private final ItemMapper itemMapper;
    private final ContainerOptimizer containerOptimizer;
    private final ContainerEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ContainerDto createContainer(ContainerDto containerDto) {
        log.info("Creating container with destination: {}", containerDto.getDestination());
        
        // Generate container number if not provided
        if (containerDto.getContainerNumber() == null || containerDto.getContainerNumber().trim().isEmpty()) {
            containerDto.setContainerNumber(generateContainerNumber());
        } else if (containerRepository.existsByContainerNumber(containerDto.getContainerNumber())) {
            throw new BusinessException(
                    "Container with number already exists: " + containerDto.getContainerNumber(),
                    BusinessException.ERROR_ITEM_ALREADY_EXISTS
            );
        }
        
        Container container = containerMapper.toEntity(containerDto);
        container.initialize();
        
        Container savedContainer = containerRepository.save(container);
        log.info("Container created successfully: {}", savedContainer.getId());
        
        return containerMapper.toDto(savedContainer);
    }
    
    @Override
    public ContainerDto getContainerById(String id) {
        log.debug("Getting container by ID: {}", id);
        Container container = findContainerById(id);
        return containerMapper.toDto(container);
    }
    
    @Override
    public ContainerDto getContainerByNumber(String containerNumber) {
        log.debug("Getting container by number: {}", containerNumber);
        Container container = containerRepository.findByContainerNumber(containerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Container", "containerNumber", containerNumber));
        return containerMapper.toDto(container);
    }
    
    @Override
    public Page<ContainerDto> getAllContainers(Pageable pageable) {
        log.debug("Getting all containers with pagination: {}", pageable);
        Page<Container> containersPage = containerRepository.findAll(pageable);
        return containersPage.map(containerMapper::toDto);
    }
    
    @Override
    public Page<ContainerDto> getContainersByStatus(String status, Pageable pageable) {
        log.debug("Getting containers by status: {} with pagination: {}", status, pageable);
        Page<Container> containersPage = containerRepository.findByStatus(status, pageable);
        return containersPage.map(containerMapper::toDto);
    }
    
    @Override
    public Page<ContainerDto> getContainersByDestination(String destination, Pageable pageable) {
        log.debug("Getting containers by destination: {} with pagination: {}", destination, pageable);
        Page<Container> containersPage = containerRepository.findByDestination(destination, pageable);
        return containersPage.map(containerMapper::toDto);
    }
    
    @Override
    public Page<ContainerDto> getContainersByStatusAndDestination(String status, String destination, Pageable pageable) {
        log.debug("Getting containers by status: {} and destination: {} with pagination: {}", 
                status, destination, pageable);
        Page<Container> containersPage = containerRepository.findByStatusAndDestination(status, destination, pageable);
        return containersPage.map(containerMapper::toDto);
    }
    
    @Override
    public List<ContainerDto> getContainersContainingItem(String itemId) {
        log.debug("Getting containers containing item: {}", itemId);
        List<Container> containers = containerRepository.findContainersContainingItem(itemId);
        return containers.stream()
                .map(containerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ContainerDto> getAvailableContainersForDestination(String destination) {
        log.debug("Getting available containers for destination: {}", destination);
        List<Container> containers = containerRepository.findAvailableContainersForDestination(destination);
        return containers.stream()
                .map(containerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ContainerDto addItemToContainer(String containerId, ItemDto itemDto) {
        log.info("Adding item {} to container {}", itemDto.getId(), containerId);
        
        Container container = findContainerById(containerId);
        
        // Validate container status
        if (!Container.STATUS_LOADING.equals(container.getStatus()) && 
            !Container.STATUS_CREATED.equals(container.getStatus())) {
            throw new BusinessException(
                    "Cannot add items to container in status: " + container.getStatus(),
                    BusinessException.ERROR_CONTAINER_CLOSED
            );
        }
        
        // If container is in CREATED status, transition to LOADING
        if (Container.STATUS_CREATED.equals(container.getStatus())) {
            container.setStatus(Container.STATUS_LOADING);
        }
        
        // Validate item data
        if (itemDto.getId() == null || itemDto.getTrackingId() == null) {
            throw new BusinessException(
                    "Item ID and tracking ID are required",
                    BusinessException.ERROR_INVALID_INPUT
            );
        }
        
        // Check if item already exists in any container
        List<Container> containersWithItem = containerRepository.findContainersContainingItem(itemDto.getId());
        if (!containersWithItem.isEmpty()) {
            throw new BusinessException(
                    "Item already exists in container: " + containersWithItem.get(0).getContainerNumber(),
                    BusinessException.ERROR_ITEM_ALREADY_EXISTS
            );
        }
        
        // Check if container has capacity for the item
        double itemVolume = itemDto.getLength() * itemDto.getWidth() * itemDto.getHeight();
        if (!container.hasCapacityFor(itemDto.getWeight(), itemVolume)) {
            throw new BusinessException(
                    "Container does not have capacity for this item",
                    BusinessException.ERROR_CONTAINER_FULL
            );
        }
        
        // Create and add the container item
        ContainerItem containerItem = ContainerItem.builder()
                .itemId(itemDto.getId())
                .trackingId(itemDto.getTrackingId())
                .description(itemDto.getDescription())
                .weight(itemDto.getWeight())
                .length(itemDto.getLength())
                .width(itemDto.getWidth())
                .height(itemDto.getHeight())
                .build();
        
        container.addItem(containerItem);
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Item added successfully to container: {}", updatedContainer.getId());
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    @Transactional
    public ContainerDto removeItemFromContainer(String containerId, String itemId) {
        log.info("Removing item {} from container {}", itemId, containerId);
        
        Container container = findContainerById(containerId);
        
        // Validate container status
        if (!Container.STATUS_LOADING.equals(container.getStatus())) {
            throw new BusinessException(
                    "Cannot remove items from container in status: " + container.getStatus(),
                    BusinessException.ERROR_CONTAINER_CLOSED
            );
        }
        
        // Find and remove the item
        ContainerItem itemToRemove = null;
        for (ContainerItem item : container.getItems()) {
            if (item.getItemId().equals(itemId)) {
                itemToRemove = item;
                break;
            }
        }
        
        if (itemToRemove == null) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
        
        container.removeItem(itemToRemove);
        
        // If container has no items, change status back to CREATED
        if (container.getItems().isEmpty()) {
            container.setStatus(Container.STATUS_CREATED);
        }
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Item removed successfully from container: {}", updatedContainer.getId());
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    public List<ItemDto> getItemsInContainer(String containerId) {
        log.debug("Getting items in container: {}", containerId);
        
        Container container = findContainerById(containerId);
        
        return container.getItems().stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ContainerDto updateContainerStatus(String id, String status) {
        log.info("Updating container status with ID: {} to status: {}", id, status);
        
        Container container = findContainerById(id);
        
        // Validate status transition
        validateStatusTransition(container.getStatus(), status);
        
        // Update status and timestamps
        container.setStatus(status);
        
        if (Container.STATUS_CLOSED.equals(status)) {
            container.setClosedAt(LocalDateTime.now());
        } else if (Container.STATUS_DISPATCHED.equals(status)) {
            container.setDispatchedAt(LocalDateTime.now());
        } else if (Container.STATUS_DELIVERED.equals(status)) {
            container.setDeliveredAt(LocalDateTime.now());
        }
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Container status updated successfully: {}", updatedContainer.getId());
        
        // Publish event for status change
        publishContainerStatusEvent(updatedContainer);
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    @Transactional
    public ContainerDto closeContainer(String id) {
        log.info("Closing container with ID: {}", id);
        
        Container container = findContainerById(id);
        
        // Validate that the container can be closed
        if (!container.canBeClosed()) {
            throw new BusinessException(
                    "Container cannot be closed. It must be in LOADING status and contain items.",
                    BusinessException.ERROR_INVALID_STATUS_TRANSITION
            );
        }
        
        // Update status and timestamp
        container.setStatus(Container.STATUS_CLOSED);
        container.setClosedAt(LocalDateTime.now());
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Container closed successfully: {}", updatedContainer.getId());
        
        // Publish event for container closure
        publishContainerStatusEvent(updatedContainer);
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    @Transactional
    public ContainerDto dispatchContainer(String id) {
        log.info("Dispatching container with ID: {}", id);
        
        Container container = findContainerById(id);
        
        // Validate that the container can be dispatched
        if (!container.canBeDispatched()) {
            throw new BusinessException(
                    "Container cannot be dispatched. It must be in CLOSED status.",
                    BusinessException.ERROR_INVALID_STATUS_TRANSITION
            );
        }
        
        // Update status and timestamp
        container.setStatus(Container.STATUS_DISPATCHED);
        container.setDispatchedAt(LocalDateTime.now());
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Container dispatched successfully: {}", updatedContainer.getId());
        
        // Publish event for container dispatch
        publishContainerStatusEvent(updatedContainer);
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    @Transactional
    public ContainerDto markContainerDelivered(String id) {
        log.info("Marking container as delivered with ID: {}", id);
        
        Container container = findContainerById(id);
        
        // Validate that the container can be marked as delivered
        if (!Container.STATUS_DISPATCHED.equals(container.getStatus())) {
            throw new BusinessException(
                    "Container cannot be marked as delivered. It must be in DISPATCHED status.",
                    BusinessException.ERROR_INVALID_STATUS_TRANSITION
            );
        }
        
        // Update status and timestamp
        container.setStatus(Container.STATUS_DELIVERED);
        container.setDeliveredAt(LocalDateTime.now());
        
        Container updatedContainer = containerRepository.save(container);
        log.info("Container marked as delivered successfully: {}", updatedContainer.getId());
        
        // Publish event for container delivery
        publishContainerStatusEvent(updatedContainer);
        
        return containerMapper.toDto(updatedContainer);
    }
    
    @Override
    @Transactional
    public void deleteContainer(String id) {
        log.info("Deleting container with ID: {}", id);
        
        Container container = findContainerById(id);
        
        // Only allow deletion of empty containers
        if (!container.isEmpty()) {
            throw new BusinessException(
                    "Cannot delete container that contains items",
                    BusinessException.ERROR_CONTAINER_NOT_EMPTY
            );
        }
        
        containerRepository.delete(container);
        log.info("Container deleted successfully: {}", id);
    }
    
    @Override
    public ContainerDto findOptimalContainerForItem(ItemDto itemDto) {
        log.info("Finding optimal container for item: {}", itemDto.getId());
        
        // Get available containers for the item's destination
        List<Container> availableContainers = containerRepository.findAvailableContainersForDestination(
                itemDto.getDestination());
        
        if (availableContainers.isEmpty()) {
            log.info("No available containers found for destination: {}", itemDto.getDestination());
            return null;
        }
        
        // Use the container optimizer to find the best container
        Container optimalContainer = containerOptimizer.findOptimalContainer(
                availableContainers, 
                itemDto.getWeight(), 
                itemDto.getLength() * itemDto.getWidth() * itemDto.getHeight());
        
        if (optimalContainer == null) {
            log.info("No suitable container found for item: {}", itemDto.getId());
            return null;
        }
        
        log.info("Found optimal container: {} for item: {}", optimalContainer.getId(), itemDto.getId());
        return containerMapper.toDto(optimalContainer);
    }
    
    @Override
    public Map<String, Long> getContainerCountsByStatus() {
        log.debug("Getting container counts by status");
        
        Map<String, Long> counts = new HashMap<>();
        counts.put(Container.STATUS_CREATED, containerRepository.countByStatus(Container.STATUS_CREATED));
        counts.put(Container.STATUS_LOADING, containerRepository.countByStatus(Container.STATUS_LOADING));
        counts.put(Container.STATUS_CLOSED, containerRepository.countByStatus(Container.STATUS_CLOSED));
        counts.put(Container.STATUS_DISPATCHED, containerRepository.countByStatus(Container.STATUS_DISPATCHED));
        counts.put(Container.STATUS_DELIVERED, containerRepository.countByStatus(Container.STATUS_DELIVERED));
        
        return counts;
    }
    
    @Override
    public Map<String, Map<String, Double>> getAverageUtilizationByDestination() {
        log.debug("Getting average utilization by destination");
        
        Map<String, Map<String, Double>> result = new HashMap<>();
        
        // Get average volume utilization
        List<Object[]> volumeData = containerRepository.getAverageVolumeUtilizationByDestination();
        for (Object[] row : volumeData) {
            String destination = (String) row[0];
            Double avgUtilization = (Double) row[1];
            
            if (!result.containsKey(destination)) {
                result.put(destination, new HashMap<>());
            }
            
            result.get(destination).put("volumeUtilization", avgUtilization);
        }
        
        // Get average weight utilization
        List<Object[]> weightData = containerRepository.getAverageWeightUtilizationByDestination();
        for (Object[] row : weightData) {
            String destination = (String) row[0];
            Double avgUtilization = (Double) row[1];
            
            if (!result.containsKey(destination)) {
                result.put(destination, new HashMap<>());
            }
            
            result.get(destination).put("weightUtilization", avgUtilization);
        }
        
        return result;
    }
    
    @Override
    public List<ContainerDto> getContainersWithHighUtilization() {
        log.debug("Getting containers with high utilization");
        
        List<Container> containers = containerRepository.findContainersWithHighUtilization();
        return containers.stream()
                .map(containerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ContainerDto> getContainersDispatchedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Getting containers dispatched between {} and {}", startTime, endTime);
        
        List<Container> containers = containerRepository.findByDispatchedAtBetween(startTime, endTime);
        return containers.stream()
                .map(containerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Find a container by its ID.
     * 
     * @param id The container ID
     * @return The container
     * @throws ResourceNotFoundException if the container is not found
     */
    private Container findContainerById(String id) {
        return containerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Container", "id", id));
    }
    
    /**
     * Generate a unique container number.
     * 
     * @return A new container number
     */
    private String generateContainerNumber() {
        return "CNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
            case Container.STATUS_CREATED -> 
                newStatus.equals(Container.STATUS_LOADING);
            case Container.STATUS_LOADING -> 
                newStatus.equals(Container.STATUS_CLOSED);
            case Container.STATUS_CLOSED -> 
                newStatus.equals(Container.STATUS_DISPATCHED);
            case Container.STATUS_DISPATCHED -> 
                newStatus.equals(Container.STATUS_DELIVERED);
            case Container.STATUS_DELIVERED -> 
                false; // Terminal state, no valid transitions
            default -> false;
        };
        
        if (!isValid) {
            throw new BusinessException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus,
                    BusinessException.ERROR_INVALID_STATUS_TRANSITION
            );
        }
    }
    
    /**
     * Publish a container status event.
     * 
     * @param container The container whose status has changed
     */
    private void publishContainerStatusEvent(Container container) {
        ContainerStatusEvent event = ContainerStatusEvent.builder()
                .id(container.getId())
                .containerNumber(container.getContainerNumber())
                .destination(container.getDestination())
                .currentWeight(container.getCurrentWeight())
                .maxWeight(container.getMaxWeight())
                .currentVolume(container.getCurrentVolume())
                .maxVolume(container.getMaxVolume())
                .itemCount(container.getItemCount())
                .status(container.getStatus())
                .itemIds(container.getItems().stream()
                        .map(ContainerItem::getItemId)
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();
        
        eventPublisher.publishContainerStatusEvent(event);
    }
}
