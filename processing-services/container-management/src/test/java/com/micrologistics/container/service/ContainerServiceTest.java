package com.micrologistics.container.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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

import com.micrologistics.common.dto.ContainerDto;
import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.container.entity.Container;
import com.micrologistics.container.entity.ContainerItem;
import com.micrologistics.container.mapper.ContainerMapper;
import com.micrologistics.container.mapper.ItemMapper;
import com.micrologistics.container.messaging.publisher.ContainerEventPublisher;
import com.micrologistics.container.optimizer.ContainerOptimizer;
import com.micrologistics.container.repository.ContainerRepository;
import com.micrologistics.container.service.impl.ContainerServiceImpl;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    @Mock
    private ContainerRepository containerRepository;
    
    @Mock
    private ContainerMapper containerMapper;
    
    @Mock
    private ItemMapper itemMapper;
    
    @Mock
    private ContainerOptimizer containerOptimizer;
    
    @Mock
    private ContainerEventPublisher eventPublisher;
    
    @InjectMocks
    private ContainerServiceImpl containerService;
    
    private Container testContainer;
    private ContainerDto testContainerDto;
    private ItemDto testItemDto;
    private ContainerItem testContainerItem;
    
    @BeforeEach
    void setUp() {
        testContainer = Container.builder()
                .id("1")
                .containerNumber("CNT-12345678")
                .destination("New York")
                .maxWeight(1000.0)
                .maxVolume(500.0)
                .currentWeight(0.0)
                .currentVolume(0.0)
                .itemCount(0)
                .status(Container.STATUS_CREATED)
                .items(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .build();
        
        testContainerDto = ContainerDto.builder()
                .id("1")
                .containerNumber("CNT-12345678")
                .destination("New York")
                .maxWeight(1000.0)
                .maxVolume(500.0)
                .currentWeight(0.0)
                .currentVolume(0.0)
                .itemCount(0)
                .status(Container.STATUS_CREATED)
                .createdAt(LocalDateTime.now())
                .build();
        
        testItemDto = ItemDto.builder()
                .id("item1")
                .trackingId("TRK-12345678")
                .description("Test Item")
                .weight(10.0)
                .length(5.0)
                .width(4.0)
                .height(3.0)
                .destination("New York")
                .build();
        
        testContainerItem = ContainerItem.builder()
                .id("ci1")
                .itemId("item1")
                .trackingId("TRK-12345678")
                .description("Test Item")
                .weight(10.0)
                .length(5.0)
                .width(4.0)
                .height(3.0)
                .build();
    }
    
    @Test
    void createContainer_Success() {
        // Arrange
        when(containerRepository.existsByContainerNumber(anyString())).thenReturn(false);
        when(containerMapper.toEntity(any(ContainerDto.class))).thenReturn(testContainer);
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        ContainerDto result = containerService.createContainer(testContainerDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(testContainerDto.getId(), result.getId());
        assertEquals(testContainerDto.getContainerNumber(), result.getContainerNumber());
        verify(containerRepository).save(any(Container.class));
    }
    
    @Test
    void createContainer_DuplicateContainerNumber() {
        // Arrange
        when(containerRepository.existsByContainerNumber(anyString())).thenReturn(true);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            containerService.createContainer(testContainerDto);
        });
        verify(containerRepository, never()).save(any(Container.class));
    }
    
    @Test
    void getContainerById_Success() {
        // Arrange
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        ContainerDto result = containerService.getContainerById("1");
        
        // Assert
        assertNotNull(result);
        assertEquals(testContainerDto.getId(), result.getId());
        verify(containerRepository).findById("1");
    }
    
    @Test
    void getContainerById_NotFound() {
        // Arrange
        when(containerRepository.findById(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            containerService.getContainerById("nonexistent");
        });
    }
    
    @Test
    void getContainerByNumber_Success() {
        // Arrange
        when(containerRepository.findByContainerNumber(anyString())).thenReturn(Optional.of(testContainer));
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        ContainerDto result = containerService.getContainerByNumber("CNT-12345678");
        
        // Assert
        assertNotNull(result);
        assertEquals(testContainerDto.getContainerNumber(), result.getContainerNumber());
        verify(containerRepository).findByContainerNumber("CNT-12345678");
    }
    
    @Test
    void getAllContainers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Container> containers = Arrays.asList(testContainer);
        Page<Container> containerPage = new PageImpl<>(containers, pageable, containers.size());
        when(containerRepository.findAll(pageable)).thenReturn(containerPage);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        Page<ContainerDto> result = containerService.getAllContainers(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testContainerDto.getId(), result.getContent().get(0).getId());
        verify(containerRepository).findAll(pageable);
    }
    
    @Test
    void addItemToContainer_Success() {
        // Arrange
        // Change container status to LOADING for this test
        testContainer.setStatus(Container.STATUS_LOADING);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        when(containerRepository.findContainersContainingItem(anyString())).thenReturn(Collections.emptyList());
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        ContainerDto result = containerService.addItemToContainer("1", testItemDto);
        
        // Assert
        assertNotNull(result);
        verify(containerRepository).findById("1");
        verify(containerRepository).findContainersContainingItem(testItemDto.getId());
        verify(containerRepository).save(any(Container.class));
    }
    
    @Test
    void addItemToContainer_ContainerClosed() {
        // Arrange
        testContainer.setStatus(Container.STATUS_CLOSED);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            containerService.addItemToContainer("1", testItemDto);
        });
    }
    
    @Test
    void addItemToContainer_ItemAlreadyInContainer() {
        // Arrange
        testContainer.setStatus(Container.STATUS_LOADING);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        when(containerRepository.findContainersContainingItem(anyString())).thenReturn(Arrays.asList(testContainer));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            containerService.addItemToContainer("1", testItemDto);
        });
    }
    
    @Test
    void updateContainerStatus_Success() {
        // Arrange
        testContainer.setStatus(Container.STATUS_LOADING);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        doNothing().when(eventPublisher).publishContainerStatusEvent(any());
        
        // Act
        ContainerDto result = containerService.updateContainerStatus("1", Container.STATUS_CLOSED);
        
        // Assert
        assertNotNull(result);
        verify(containerRepository).findById("1");
        verify(containerRepository).save(any(Container.class));
        verify(eventPublisher).publishContainerStatusEvent(any());
    }
    
    @Test
    void updateContainerStatus_InvalidTransition() {
        // Arrange
        testContainer.setStatus(Container.STATUS_CREATED);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            containerService.updateContainerStatus("1", Container.STATUS_DISPATCHED);
        });
    }
    
    @Test
    void closeContainer_Success() {
        // Arrange
        testContainer.setStatus(Container.STATUS_LOADING);
        testContainer.addItem(testContainerItem);
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        doNothing().when(eventPublisher).publishContainerStatusEvent(any());
        
        // Act
        ContainerDto result = containerService.closeContainer("1");
        
        // Assert
        assertNotNull(result);
        verify(containerRepository).findById("1");
        verify(containerRepository).save(any(Container.class));
        verify(eventPublisher).publishContainerStatusEvent(any());
    }
    
    @Test
    void closeContainer_EmptyContainer() {
        // Arrange
        testContainer.setStatus(Container.STATUS_LOADING);
        // Container has no items
        
        when(containerRepository.findById(anyString())).thenReturn(Optional.of(testContainer));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            containerService.closeContainer("1");
        });
    }
    
    @Test
    void findOptimalContainerForItem_Success() {
        // Arrange
        List<Container> availableContainers = Arrays.asList(testContainer);
        
        when(containerRepository.findAvailableContainersForDestination(anyString())).thenReturn(availableContainers);
        when(containerOptimizer.findOptimalContainer(anyList(), anyDouble(), anyDouble())).thenReturn(testContainer);
        when(containerMapper.toDto(any(Container.class))).thenReturn(testContainerDto);
        
        // Act
        ContainerDto result = containerService.findOptimalContainerForItem(testItemDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(testContainerDto.getId(), result.getId());
        verify(containerRepository).findAvailableContainersForDestination(testItemDto.getDestination());
        verify(containerOptimizer).findOptimalContainer(anyList(), anyDouble(), anyDouble());
    }
    
    @Test
    void findOptimalContainerForItem_NoContainersAvailable() {
        // Arrange
        when(containerRepository.findAvailableContainersForDestination(anyString())).thenReturn(Collections.emptyList());
        
        // Act
        ContainerDto result = containerService.findOptimalContainerForItem(testItemDto);
        
        // Assert
        assertNull(result);
        verify(containerRepository).findAvailableContainersForDestination(testItemDto.getDestination());
        verify(containerOptimizer, never()).findOptimalContainer(anyList(), anyDouble(), anyDouble());
    }
}
