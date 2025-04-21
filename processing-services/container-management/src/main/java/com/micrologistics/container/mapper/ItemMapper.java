package com.micrologistics.container.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.container.entity.ContainerItem;

/**
 * Mapper class for converting between ContainerItem entities and ItemDtos.
 */
@Component
public class ItemMapper {
    
    /**
     * Convert a ContainerItem entity to an ItemDto.
     * 
     * @param containerItem The ContainerItem entity
     * @return The ItemDto
     */
    public ItemDto toDto(ContainerItem containerItem) {
        if (containerItem == null) {
            return null;
        }
        
        return ItemDto.builder()
                .id(containerItem.getItemId())
                .trackingId(containerItem.getTrackingId())
                .description(containerItem.getDescription())
                .weight(containerItem.getWeight())
                .length(containerItem.getLength())
                .width(containerItem.getWidth())
                .height(containerItem.getHeight())
                .build();
    }
    
    /**
     * Convert a list of ContainerItem entities to a list of ItemDtos.
     * 
     * @param containerItems The list of ContainerItem entities
     * @return The list of ItemDtos
     */
    public List<ItemDto> toDtoList(List<ContainerItem> containerItems) {
        if (containerItems == null) {
            return null;
        }
        
        return containerItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
