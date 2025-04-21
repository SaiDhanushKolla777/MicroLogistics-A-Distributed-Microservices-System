package com.micrologistics.item.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.item.entity.Item;
import com.micrologistics.item.mapper.ItemMapper;
import com.micrologistics.item.messaging.publisher.ItemEventPublisher;
import com.micrologistics.item.repository.ItemRepository;
import com.micrologistics.item.service.impl.ItemRegistrationServiceImpl;

@ExtendWith(MockitoExtension.class)
class ItemRegistrationServiceTest {

    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemMapper itemMapper;
    
    @Mock
    private ItemEventPublisher eventPublisher;
    
    @InjectMocks
    private ItemRegistrationServiceImpl itemService;
    
    private Item testItem;
    private ItemDto testItemDto;
    
    @BeforeEach
    void setUp() {
        testItem = Item.builder()
                .id("1")
                .trackingId("TRK-12345678")
                .description("Test Item")
                .weight(10.0)
                .length(20.0)
                .width(15.0)
                .height(5.0)
                .destination("New York")
                .status(Item.STATUS_REGISTERED)
                .priority(1)
                .registeredAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        testItemDto = ItemDto.builder()
                .id("1")
                .trackingId("TRK-12345678")
                .description("Test Item")
                .weight(10.0)
                .length(20.0)
                .width(15.0)
                .height(5.0)
                .destination("New York")
                .status(Item.STATUS_REGISTERED)
                .priority(1)
                .registeredAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void registerItem_Success() {
        // Arrange
        when(itemRepository.existsByTrackingId(anyString())).thenReturn(false);
        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(testItem);
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        doNothing().when(eventPublisher).publishItemRegisteredEvent(any(Item.class));
        
        // Act
        ItemDto result = itemService.registerItem(testItemDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(testItemDto.getDescription(), result.getDescription());
        assertEquals(Item.STATUS_REGISTERED, result.getStatus());
        verify(itemRepository).save(any(Item.class));
        verify(eventPublisher).publishItemRegisteredEvent(any(Item.class));
    }
    
    @Test
    void registerItem_DuplicateTrackingId() {
        // Arrange
        when(itemRepository.existsByTrackingId(anyString())).thenReturn(true);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            itemService.registerItem(testItemDto);
        });
    }
    
    @Test
    void getItemById_Success() {
        // Arrange
        when(itemRepository.findById(anyString())).thenReturn(Optional.of(testItem));
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        
        // Act
        ItemDto result = itemService.getItemById("1");
        
        // Assert
        assertNotNull(result);
        assertEquals(testItemDto.getId(), result.getId());
        verify(itemRepository).findById("1");
    }
    
    @Test
    void getItemById_NotFound() {
        // Arrange
        when(itemRepository.findById(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            itemService.getItemById("nonexistent");
        });
    }
    
    @Test
    void getItemByTrackingId_Success() {
        // Arrange
        when(itemRepository.findByTrackingId(anyString())).thenReturn(Optional.of(testItem));
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        
        // Act
        ItemDto result = itemService.getItemByTrackingId("TRK-12345678");
        
        // Assert
        assertNotNull(result);
        assertEquals(testItemDto.getTrackingId(), result.getTrackingId());
        verify(itemRepository).findByTrackingId("TRK-12345678");
    }
    
    @Test
    void getAllItems_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = Arrays.asList(testItem);
        Page<Item> itemPage = new PageImpl<>(items, pageable, items.size());
        when(itemRepository.findAll(pageable)).thenReturn(itemPage);
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        
        // Act
        Page<ItemDto> result = itemService.getAllItems(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testItemDto.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void updateItem_Success() {
        // Arrange
        when(itemRepository.findById(anyString())).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        
        ItemDto updateDto = ItemDto.builder()
                .description("Updated Description")
                .weight(15.0)
                .destination("Boston")
                .build();
        
        // Act
        ItemDto result = itemService.updateItem("1", updateDto);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).findById("1");
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void updateItemStatus_Success() {
        // Arrange
        testItem.setStatus(Item.STATUS_REGISTERED);
        when(itemRepository.findById(anyString())).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(itemMapper.toDto(any(Item.class))).thenReturn(testItemDto);
        
        // Act
        ItemDto result = itemService.updateItemStatus("1", Item.STATUS_ROUTING);
        
        // Assert
        assertNotNull(result);
        verify(itemRepository).findById("1");
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void updateItemStatus_InvalidTransition() {
        // Arrange
        testItem.setStatus(Item.STATUS_REGISTERED);
        when(itemRepository.findById(anyString())).thenReturn(Optional.of(testItem));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            itemService.updateItemStatus("1", Item.STATUS_SHIPPED);
        });
    }
    
    @Test
    void deleteItem_Success() {
        // Arrange
        when(itemRepository.existsById(anyString())).thenReturn(true);
        doNothing().when(itemRepository).deleteById(anyString());
        
        // Act
        itemService.deleteItem("1");
        
        // Assert
        verify(itemRepository).existsById("1");
        verify(itemRepository).deleteById("1");
    }
    
    @Test
    void deleteItem_NotFound() {
        // Arrange
        when(itemRepository.existsById(anyString())).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            itemService.deleteItem("nonexistent");
        });
    }
    
    @Test
    void getItemCountsByStatus_Success() {
        // Arrange
        when(itemRepository.countByStatus(Item.STATUS_REGISTERED)).thenReturn(10L);
        when(itemRepository.countByStatus(Item.STATUS_ROUTING)).thenReturn(5L);
        when(itemRepository.countByStatus(Item.STATUS_PROCESSING)).thenReturn(3L);
        when(itemRepository.countByStatus(Item.STATUS_CONTAINERIZED)).thenReturn(2L);
        when(itemRepository.countByStatus(Item.STATUS_SHIPPED)).thenReturn(1L);
        
        // Act
        Map<String, Long> result = itemService.getItemCountsByStatus();
        
        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(10L, result.get(Item.STATUS_REGISTERED));
        assertEquals(5L, result.get(Item.STATUS_ROUTING));
        assertEquals(3L, result.get(Item.STATUS_PROCESSING));
        assertEquals(2L, result.get(Item.STATUS_CONTAINERIZED));
        assertEquals(1L, result.get(Item.STATUS_SHIPPED));
    }
}
