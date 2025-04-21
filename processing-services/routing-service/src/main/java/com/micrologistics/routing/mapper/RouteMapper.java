package com.micrologistics.routing.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.routing.entity.Route;

/**
 * Mapper class for converting between Route entities and DTOs.
 */
@Component
public class RouteMapper {
    
    /**
     * Convert a Route entity to a RouteDto.
     * 
     * @param route The Route entity
     * @return The RouteDto
     */
    public RouteDto toDto(Route route) {
        if (route == null) {
            return null;
        }
        
        return RouteDto.builder()
                .id(route.getId())
                .itemId(route.getItemId())
                .trackingId(route.getTrackingId())
                .routeSteps(route.getRouteSteps())
                .currentStep(route.getCurrentStep())
                .status(route.getStatus())
                .estimatedTimeMinutes(route.getEstimatedTimeMinutes())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .estimatedCompletionTime(route.getEstimatedCompletionTime())
                .build();
    }
    
    /**
     * Convert a RouteDto to a Route entity.
     * 
     * @param routeDto The RouteDto
     * @return The Route entity
     */
    public Route toEntity(RouteDto routeDto) {
        if (routeDto == null) {
            return null;
        }
        
        return Route.builder()
                .id(routeDto.getId())
                .itemId(routeDto.getItemId())
                .trackingId(routeDto.getTrackingId())
                .routeSteps(routeDto.getRouteSteps())
                .currentStep(routeDto.getCurrentStep())
                .status(routeDto.getStatus())
                .estimatedTimeMinutes(routeDto.getEstimatedTimeMinutes())
                .createdAt(routeDto.getCreatedAt())
                .updatedAt(routeDto.getUpdatedAt())
                .estimatedCompletionTime(routeDto.getEstimatedCompletionTime())
                .build();
    }
    
    /**
     * Convert a list of Route entities to a list of RouteDtos.
     * 
     * @param routes The list of Route entities
     * @return The list of RouteDtos
     */
    public List<RouteDto> toDtoList(List<Route> routes) {
        if (routes == null) {
            return null;
        }
        
        return routes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
