package com.micrologistics.item.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.item.entity.Item;

/**
 * Mapper class for converting between Item entities and DTOs.
 */
@Component
public class ItemMapper {
    
    /**
     * Convert an Item entity to an ItemDto.
     * 
     * @param item The Item entity
     * @return The ItemDto
     */
    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        
        return ItemDto.builder()
                .id(item.getId())
                .trackingId(item.getTrackingId())
                .description(item.getDescription())
                .weight(item.getWeight())
                .length(item.getLength())
                .width(item.getWidth())
                .height(item.getHeight())
                .destination(item.getDestination())
                .status(item.getStatus())
                .priority(item.getPriority())
                .registeredAt(item.getRegisteredAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert an ItemDto to an Item entity.
     * 
     * @param itemDto The ItemDto
     * @return The Item entity
     */
    public Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        
        return Item.builder()
                .id(itemDto.getId())
                .trackingId(itemDto.getTrackingId())
                .description(itemDto.getDescription())
                .weight(itemDto.getWeight())
                .length(itemDto.getLength())
                .width(itemDto.getWidth())
                .height(itemDto.getHeight())
                .destination(itemDto.getDestination())
                .status(itemDto.getStatus())
                .priority(itemDto.getPriority())
                .registeredAt(itemDto.getRegisteredAt())
                .updatedAt(itemDto.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert a list of Item entities to a list of ItemDtos.
     * 
     * @param items The list of Item entities
     * @return The list of ItemDtos
     */
    public List<ItemDto> toDtoList(List<Item> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert an Item entity to an ItemRegisteredEvent.
     * 
     * @param item The Item entity
     * @return The ItemRegisteredEvent
     */
    public ItemRegisteredEvent toEvent(Item item) {
        if (item == null) {
            return null;
        }
        
        return ItemRegisteredEvent.builder()
                .id(item.getId())
                .trackingId(item.getTrackingId())
                .description(item.getDescription())
                .weight(item.getWeight())
                .length(item.getLength())
                .width(item.getWidth())
                .height(item.getHeight())
                .destination(item.getDestination())
                .priority(item.getPriority())
                .timestamp(item.getRegisteredAt())
                .build();
    }
}
