package com.micrologistics.container.optimizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.micrologistics.container.entity.Container;

@ExtendWith(MockitoExtension.class)
class ContainerOptimizerTest {

    @Mock
    private PackingAlgorithm packingAlgorithm;
    
    @InjectMocks
    private ContainerOptimizer containerOptimizer;
    
    private Container container1;
    private Container container2;
    private List<Container> containers;
    
    @BeforeEach
    void setUp() {
        container1 = Container.builder()
                .id("1")
                .containerNumber("CNT-12345678")
                .destination("New York")
                .maxWeight(1000.0)
                .maxVolume(500.0)
                .currentWeight(0.0)
                .currentVolume(0.0)
                .build();
        
        container2 = Container.builder()
                .id("2")
                .containerNumber("CNT-87654321")
                .destination("New York")
                .maxWeight(2000.0)
                .maxVolume(1000.0)
                .currentWeight(500.0)
                .currentVolume(200.0)
                .build();
        
        containers = Arrays.asList(container1, container2);
    }
    
    @Test
    void findOptimalContainer_Success() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 50.0;
        
        when(packingAlgorithm.findBestContainer(anyList(), anyDouble(), anyDouble())).thenReturn(container1);
        
        // Act
        Container result = containerOptimizer.findOptimalContainer(containers, itemWeight, itemVolume);
        
        // Assert
        assertNotNull(result);
        assertEquals(container1.getId(), result.getId());
        verify(packingAlgorithm).findBestContainer(anyList(), eq(itemWeight), eq(itemVolume));
    }
    
    @Test
    void findOptimalContainer_NoContainers() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 50.0;
        
        // Act
        Container result = containerOptimizer.findOptimalContainer(null, itemWeight, itemVolume);
        
        // Assert
        assertNull(result);
        verify(packingAlgorithm, never()).findBestContainer(anyList(), anyDouble(), anyDouble());
    }
    
    @Test
    void findOptimalContainer_EmptyContainerList() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 50.0;
        
        // Act
        Container result = containerOptimizer.findOptimalContainer(Collections.emptyList(), itemWeight, itemVolume);
        
        // Assert
        assertNull(result);
        verify(packingAlgorithm, never()).findBestContainer(anyList(), anyDouble(), anyDouble());
    }
    
    @Test
    void findOptimalContainer_NoSuitableContainers() {
        // Arrange
        double itemWeight = 10000.0; // Too heavy for any container
        double itemVolume = 50.0;
        
        Container container1WithoutCapacity = container1;
        container1WithoutCapacity.setCurrentWeight(1000.0); // Already at max weight
        
        Container container2WithoutCapacity = container2;
        container2WithoutCapacity.setCurrentWeight(2000.0); // Already at max weight
        
        List<Container> containersWithoutCapacity = Arrays.asList(
                container1WithoutCapacity, container2WithoutCapacity);
        
        // Act
        Container result = containerOptimizer.findOptimalContainer(containersWithoutCapacity, itemWeight, itemVolume);
        
        // Assert
        assertNull(result);
        verify(packingAlgorithm, never()).findBestContainer(anyList(), anyDouble(), anyDouble());
    }
    
    @Test
    void canFitInContainer_Success() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 50.0;
        
        // Act
        boolean result = containerOptimizer.canFitInContainer(container1, itemWeight, itemVolume);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void canFitInContainer_TooHeavy() {
        // Arrange
        double itemWeight = 1500.0; // Exceeds container1's max weight
        double itemVolume = 50.0;
        
        // Act
        boolean result = containerOptimizer.canFitInContainer(container1, itemWeight, itemVolume);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void canFitInContainer_TooLarge() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 600.0; // Exceeds container1's max volume
        
        // Act
        boolean result = containerOptimizer.canFitInContainer(container1, itemWeight, itemVolume);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void canFitInContainer_NullContainer() {
        // Arrange
        double itemWeight = 100.0;
        double itemVolume = 50.0;
        
        // Act
        boolean result = containerOptimizer.canFitInContainer(null, itemWeight, itemVolume);
        
        // Assert
        assertFalse(result);
    }
}
