package com.micrologistics.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ItemDtoTest {

    @Test
    void testItemDtoBuilder() {
        // Arrange & Act
        LocalDateTime now = LocalDateTime.now();
        ItemDto item = ItemDto.builder()
                .id("id1")
                .trackingId("TRK123456")
                .description("Test item")
                .weight(10.5)
                .length(20.0)
                .width(15.0)
                .height(5.0)
                .destination("New York")
                .status("REGISTERED")
                .priority(1)
                .registeredAt(now)
                .updatedAt(now)
                .build();
        
        // Assert
        assertEquals("id1", item.getId());
        assertEquals("TRK123456", item.getTrackingId());
        assertEquals("Test item", item.getDescription());
        assertEquals(10.5, item.getWeight());
        assertEquals(20.0, item.getLength());
        assertEquals(15.0, item.getWidth());
        assertEquals(5.0, item.getHeight());
        assertEquals("New York", item.getDestination());
        assertEquals("REGISTERED", item.getStatus());
        assertEquals(1, item.getPriority());
        assertEquals(now, item.getRegisteredAt());
        assertEquals(now, item.getUpdatedAt());
    }
    
    @Test
    void testGetVolume() {
        // Arrange
        ItemDto item = new ItemDto();
        item.setLength(10.0);
        item.setWidth(5.0);
        item.setHeight(2.0);
        
        // Act
        Double volume = item.getVolume();
        
        // Assert
        assertEquals(100.0, volume);
    }
    
    @Test
    void testGetVolumeWithNullDimensions() {
        // Arrange
        ItemDto item = new ItemDto();
        item.setLength(null);
        item.setWidth(5.0);
        item.setHeight(2.0);
        
        // Act
        Double volume = item.getVolume();
        
        // Assert
        assertNull(volume);
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ItemDto item1 = ItemDto.builder()
                .id("id1")
                .trackingId("TRK123456")
                .description("Test item")
                .build();
        
        ItemDto item2 = ItemDto.builder()
                .id("id1")
                .trackingId("TRK123456")
                .description("Test item")
                .build();
        
        ItemDto item3 = ItemDto.builder()
                .id("id2")
                .trackingId("TRK789012")
                .description("Different item")
                .build();
        
        // Act & Assert
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());
    }
}
