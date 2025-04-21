package com.micrologistics.container.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.micrologistics.common.dto.ContainerDto;
import com.micrologistics.container.entity.Container;

/**
 * Mapper class for converting between Container entities and DTOs.
 */
@Component
public class ContainerMapper {
    
    /**
     * Convert a Container entity to a ContainerDto.
     * 
     * @param container The Container entity
     * @return The ContainerDto
     */
    public ContainerDto toDto(Container container) {
        if (container == null) {
            return null;
        }
        
        return ContainerDto.builder()
                .id(container.getId())
                .containerNumber(container.getContainerNumber())
                .destination(container.getDestination())
                .maxWeight(container.getMaxWeight())
                .maxVolume(container.getMaxVolume())
                .currentWeight(container.getCurrentWeight())
                .currentVolume(container.getCurrentVolume())
                .itemCount(container.getItemCount())
                .status(container.getStatus())
                .itemIds(container.getItemIds())
                .createdAt(container.getCreatedAt())
                .closedAt(container.getClosedAt())
                .dispatchedAt(container.getDispatchedAt())
                .build();
    }
    
    /**
     * Convert a ContainerDto to a Container entity.
     * 
     * @param containerDto The ContainerDto
     * @return The Container entity
     */
    public Container toEntity(ContainerDto containerDto) {
        if (containerDto == null) {
            return null;
        }
        
        Container container = Container.builder()
                .id(containerDto.getId())
                .containerNumber(containerDto.getContainerNumber())
                .destination(containerDto.getDestination())
                .maxWeight(containerDto.getMaxWeight())
                .maxVolume(containerDto.getMaxVolume())
                .currentWeight(containerDto.getCurrentWeight())
                .currentVolume(containerDto.getCurrentVolume())
                .itemCount(containerDto.getItemCount())
                .status(containerDto.getStatus())
                .createdAt(containerDto.getCreatedAt())
                .closedAt(containerDto.getClosedAt())
                .dispatchedAt(containerDto.getDispatchedAt())
                .build();
        
        // Note: We don't set the items here because they are managed separately
        // and would require additional mapping logic
        
        return container;
    }
    
    /**
     * Convert a list of Container entities to a list of ContainerDtos.
     * 
     * @param containers The list of Container entities
     * @return The list of ContainerDtos
     */
    public List<ContainerDto> toDtoList(List<Container> containers) {
        if (containers == null) {
            return null;
        }
        
        return containers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
