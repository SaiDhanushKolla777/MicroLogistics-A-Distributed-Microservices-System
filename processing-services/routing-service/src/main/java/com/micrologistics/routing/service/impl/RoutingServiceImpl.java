package com.micrologistics.routing.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.routing.algorithm.OptimalPathFinder;
import com.micrologistics.routing.entity.Route;
import com.micrologistics.routing.mapper.RouteMapper;
import com.micrologistics.routing.repository.RouteRepository;
import com.micrologistics.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the RoutingService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final OptimalPathFinder pathFinder;
    
    @Override
    @Transactional
    public RouteDto createRoute(String itemId, String trackingId, String destination, Double weight, Integer priority) {
        log.info("Creating route for item: {}, tracking: {}, destination: {}", 
                itemId, trackingId, destination);
        
        // Check if route already exists for this item
        routeRepository.findByItemId(itemId).ifPresent(existingRoute -> {
            throw new BusinessException(
                    "Route already exists for item: " + itemId,
                    BusinessException.ERROR_ITEM_ALREADY_EXISTS
            );
        });
        
        // Use the path finder to determine optimal route
        if (priority == null) {
            priority = 1; // Default priority
        }
        
        List<String> routeSteps = pathFinder.findOptimalPath(itemId, destination, weight, priority);
        if (routeSteps.isEmpty()) {
            throw new BusinessException(
                    "Failed to determine route for item: " + itemId,
                    BusinessException.ERROR_ROUTE_INVALID
            );
        }
        
        // Calculate estimated processing time
        double estimatedTimeMinutes = pathFinder.calculateEstimatedTime(routeSteps);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimatedCompletionTime = now.plusMinutes((long) estimatedTimeMinutes);
        
        Route route = Route.builder()
                .itemId(itemId)
                .trackingId(trackingId)
                .routeSteps(routeSteps)
                .currentStep(routeSteps.get(0))
                .status(Route.STATUS_CREATED)
                .estimatedTimeMinutes(estimatedTimeMinutes)
                .createdAt(now)
                .updatedAt(now)
                .estimatedCompletionTime(estimatedCompletionTime)
                .build();
        
        Route savedRoute = routeRepository.save(route);
        log.info("Created route for item {}: routeId={}, steps={}", 
                itemId, savedRoute.getId(), routeSteps);
        
        return routeMapper.toDto(savedRoute);
    }

    @Override
    public RouteDto getRouteById(String id) {
        log.debug("Getting route by ID: {}", id);
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", id));
        return routeMapper.toDto(route);
    }

    @Override
    public RouteDto getRouteByTrackingId(String trackingId) {
        log.debug("Getting route by tracking ID: {}", trackingId);
        Route route = routeRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "trackingId", trackingId));
        return routeMapper.toDto(route);
    }

    @Override
    public RouteDto getRouteByItemId(String itemId) {
        log.debug("Getting route by item ID: {}", itemId);
        Route route = routeRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "itemId", itemId));
        return routeMapper.toDto(route);
    }

    @Override
    public Page<RouteDto> getAllRoutes(Pageable pageable) {
        log.debug("Getting all routes with pagination: {}", pageable);
        Page<Route> routesPage = routeRepository.findAll(pageable);
        return routesPage.map(routeMapper::toDto);
    }

    @Override
    public Page<RouteDto> getRoutesByStatus(String status, Pageable pageable) {
        log.debug("Getting routes by status: {} with pagination: {}", status, pageable);
        Page<Route> routesPage = routeRepository.findByStatus(status, pageable);
        return routesPage.map(routeMapper::toDto);
    }

    @Override
    @Transactional
    public RouteDto updateRouteStep(String id, String step) {
        log.info("Updating route step with ID: {} to step: {}", id, step);
        
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", id));
        
        // Validate that the step is part of the route
        if (!route.getRouteSteps().contains(step)) {
            throw new BusinessException(
                    "Invalid step for route: " + step,
                    BusinessException.ERROR_ROUTE_INVALID
            );
        }
        
        // Update the current step
        route.setCurrentStep(step);
        route.setUpdatedAt(LocalDateTime.now());
        
        // Update status based on step
        if (route.isFinalStep()) {
            route.setStatus(Route.STATUS_COMPLETED);
        } else {
            route.setStatus(Route.STATUS_IN_PROGRESS);
        }
        
        Route updatedRoute = routeRepository.save(route);
        log.info("Route step updated successfully: {}, status: {}", 
                updatedRoute.getId(), updatedRoute.getStatus());
        
        return routeMapper.toDto(updatedRoute);
    }

    @Override
    @Transactional
    public RouteDto processItemRegisteredEvent(ItemRegisteredEvent event) {
        log.info("Processing item registered event for item: {}, tracking: {}", 
                event.getId(), event.getTrackingId());
        
        return createRoute(
                event.getId(),
                event.getTrackingId(),
                event.getDestination(),
                event.getWeight(),
                event.getPriority()
        );
    }

    @Override
    public Map<String, Integer> getEquipmentLoadStatus() {
        log.debug("Getting equipment load status");
        return pathFinder.getEquipmentLoadStatus();
    }

    @Override
    public Map<String, Boolean> getEquipmentOperationalStatus() {
        log.debug("Getting equipment operational status");
        return pathFinder.getEquipmentOperationalStatus();
    }

    @Override
    public void updateEquipmentStatus(String equipment, boolean isOperational) {
        log.info("Updating equipment status for {}: operational = {}", equipment, isOperational);
        pathFinder.updateEquipmentStatus(equipment, isOperational);
    }

    @Override
    public List<String> getAllFacilities() {
        return pathFinder.getAllFacilities();
    }

    @Override
    public List<RouteDto> getDelayedRoutes() {
        log.debug("Getting delayed routes");
        List<Route> delayedRoutes = routeRepository.findDelayedRoutes();
        return routeMapper.toDtoList(delayedRoutes);
    }

    @Override
    public Map<String, Double> getAverageTimeByStep() {
        log.debug("Getting average time by step");
        List<Object[]> results = routeRepository.findAverageEstimatedTimeByStep();
        
        Map<String, Double> averageTimes = new HashMap<>();
        for (Object[] result : results) {
            String step = (String) result[0];
            Double avgTime = (Double) result[1];
            averageTimes.put(step, avgTime);
        }
        
        return averageTimes;
    }
}
