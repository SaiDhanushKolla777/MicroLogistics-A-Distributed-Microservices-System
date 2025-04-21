package com.micrologistics.routing.service;

import static org.junit.jupiter.api.Assertions.;
import static org.mockito.ArgumentMatchers.;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
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

import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.common.exception.BusinessException;
import com.micrologistics.common.exception.ResourceNotFoundException;
import com.micrologistics.routing.algorithm.OptimalPathFinder;
import com.micrologistics.routing.entity.Route;
import com.micrologistics.routing.mapper.RouteMapper;
import com.micrologistics.routing.repository.RouteRepository;
import com.micrologistics.routing.service.impl.RoutingServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {
	
	@Mock
	private RouteRepository routeRepository;

	@Mock
	private RouteMapper routeMapper;

	@Mock
	private OptimalPathFinder pathFinder;

	@InjectMocks
	private RoutingServiceImpl routingService;

	private Route testRoute;
	private RouteDto testRouteDto;
	private List<String> routeSteps;

	@BeforeEach
	void setUp() {
	    routeSteps = Arrays.asList("INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A", "PACKAGING_AREA");
	    
	    testRoute = Route.builder()
	            .id("1")
	            .itemId("item1")
	            .trackingId("TRK-12345678")
	            .routeSteps(routeSteps)
	            .currentStep("INBOUND_DOCK")
	            .status(Route.STATUS_CREATED)
	            .estimatedTimeMinutes(30.0)
	            .createdAt(LocalDateTime.now())
	            .updatedAt(LocalDateTime.now())
	            .estimatedCompletionTime(LocalDateTime.now().plusMinutes(30))
	            .build();
	    
	    testRouteDto = RouteDto.builder()
	            .id("1")
	            .itemId("item1")
	            .trackingId("TRK-12345678")
	            .routeSteps(routeSteps)
	            .currentStep("INBOUND_DOCK")
	            .status(Route.STATUS_CREATED)
	            .estimatedTimeMinutes(30.0)
	            .createdAt(LocalDateTime.now())
	            .updatedAt(LocalDateTime.now())
	            .estimatedCompletionTime(LocalDateTime.now().plusMinutes(30))
	            .build();
	}

	@Test
	void createRoute_Success() {
	    // Arrange
	    when(routeRepository.findByItemId(anyString())).thenReturn(Optional.empty());
	    when(pathFinder.findOptimalPath(anyString(), anyString(), anyDouble(), anyInt()))
	        .thenReturn(routeSteps);
	    when(pathFinder.calculateEstimatedTime(anyList())).thenReturn(30.0);
	    when(routeRepository.save(any(Route.class))).thenReturn(testRoute);
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.createRoute("item1", "TRK-12345678", "New York", 10.0, 1);
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(testRouteDto.getId(), result.getId());
	    assertEquals(testRouteDto.getItemId(), result.getItemId());
	    verify(routeRepository).save(any(Route.class));
	}

	@Test
	void createRoute_RouteAlreadyExists() {
	    // Arrange
	    when(routeRepository.findByItemId(anyString())).thenReturn(Optional.of(testRoute));
	    
	    // Act & Assert
	    assertThrows(BusinessException.class, () -> {
	        routingService.createRoute("item1", "TRK-12345678", "New York", 10.0, 1);
	    });
	    verify(routeRepository, never()).save(any(Route.class));
	}

	@Test
	void getRouteById_Success() {
	    // Arrange
	    when(routeRepository.findById(anyString())).thenReturn(Optional.of(testRoute));
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.getRouteById("1");
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(testRouteDto.getId(), result.getId());
	    verify(routeRepository).findById("1");
	}

	@Test
	void getRouteById_NotFound() {
	    // Arrange
	    when(routeRepository.findById(anyString())).thenReturn(Optional.empty());
	    
	    // Act & Assert
	    assertThrows(ResourceNotFoundException.class, () -> {
	        routingService.getRouteById("nonexistent");
	    });
	}

	@Test
	void getRouteByTrackingId_Success() {
	    // Arrange
	    when(routeRepository.findByTrackingId(anyString())).thenReturn(Optional.of(testRoute));
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.getRouteByTrackingId("TRK-12345678");
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(testRouteDto.getTrackingId(), result.getTrackingId());
	    verify(routeRepository).findByTrackingId("TRK-12345678");
	}

	@Test
	void getRouteByItemId_Success() {
	    // Arrange
	    when(routeRepository.findByItemId(anyString())).thenReturn(Optional.of(testRoute));
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.getRouteByItemId("item1");
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(testRouteDto.getItemId(), result.getItemId());
	    verify(routeRepository).findByItemId("item1");
	}

	@Test
	void getAllRoutes_Success() {
	    // Arrange
	    Pageable pageable = PageRequest.of(0, 10);
	    List<Route> routes = Arrays.asList(testRoute);
	    Page<Route> routePage = new PageImpl<>(routes, pageable, routes.size());
	    when(routeRepository.findAll(pageable)).thenReturn(routePage);
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    Page<RouteDto> result = routingService.getAllRoutes(pageable);
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(1, result.getTotalElements());
	    assertEquals(testRouteDto.getId(), result.getContent().get(0).getId());
	    verify(routeRepository).findAll(pageable);
	}

	@Test
	void updateRouteStep_Success() {
	    // Arrange
	    testRoute.setRouteSteps(Arrays.asList("INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A"));
	    testRoute.setCurrentStep("INBOUND_DOCK");
	    
	    when(routeRepository.findById(anyString())).thenReturn(Optional.of(testRoute));
	    when(routeRepository.save(any(Route.class))).thenReturn(testRoute);
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.updateRouteStep("1", "SCANNER_STATION");
	    
	    // Assert
	    assertNotNull(result);
	    verify(routeRepository).findById("1");
	    verify(routeRepository).save(any(Route.class));
	}

	@Test
	void updateRouteStep_InvalidStep() {
	    // Arrange
	    testRoute.setRouteSteps(Arrays.asList("INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A"));
	    testRoute.setCurrentStep("INBOUND_DOCK");
	    
	    when(routeRepository.findById(anyString())).thenReturn(Optional.of(testRoute));
	    
	    // Act & Assert
	    assertThrows(BusinessException.class, () -> {
	        routingService.updateRouteStep("1", "NONEXISTENT_STEP");
	    });
	}

	@Test
	void processItemRegisteredEvent_Success() {
	    // Arrange
	    ItemRegisteredEvent event = ItemRegisteredEvent.builder()
	            .id("item1")
	            .trackingId("TRK-12345678")
	            .description("Test Item")
	            .weight(10.0)
	            .destination("New York")
	            .priority(1)
	            .timestamp(LocalDateTime.now())
	            .build();
	    
	    when(routeRepository.findByItemId(anyString())).thenReturn(Optional.empty());
	    when(pathFinder.findOptimalPath(anyString(), anyString(), anyDouble(), anyInt()))
	        .thenReturn(routeSteps);
	    when(pathFinder.calculateEstimatedTime(anyList())).thenReturn(30.0);
	    when(routeRepository.save(any(Route.class))).thenReturn(testRoute);
	    when(routeMapper.toDto(any(Route.class))).thenReturn(testRouteDto);
	    
	    // Act
	    RouteDto result = routingService.processItemRegisteredEvent(event);
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(testRouteDto.getId(), result.getId());
	    verify(routeRepository).save(any(Route.class));
	}

	@Test
	void getEquipmentLoadStatus_Success() {
	    // Arrange
	    Map<String, Integer> loadStatus = new HashMap<>();
	    loadStatus.put("INBOUND_DOCK", 50);
	    loadStatus.put("SCANNER_STATION", 30);
	    when(pathFinder.getEquipmentLoadStatus()).thenReturn(loadStatus);
	    
	    // Act
	    Map<String, Integer> result = routingService.getEquipmentLoadStatus();
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(2, result.size());
	    assertEquals(50, result.get("INBOUND_DOCK"));
	    assertEquals(30, result.get("SCANNER_STATION"));
	    verify(pathFinder).getEquipmentLoadStatus();
	}

	@Test
	void updateEquipmentStatus_Success() {
	    // Arrange
	    doNothing().when(pathFinder).updateEquipmentStatus(anyString(), anyBoolean());
	    
	    // Act
	    routingService.updateEquipmentStatus("INBOUND_DOCK", false);
	    
	    // Assert
	    verify(pathFinder).updateEquipmentStatus("INBOUND_DOCK", false);
	}

	@Test
	void getDelayedRoutes_Success() {
	    // Arrange
	    List<Route> delayedRoutes = Arrays.asList(testRoute);
	    when(routeRepository.findDelayedRoutes()).thenReturn(delayedRoutes);
	    when(routeMapper.toDtoList(anyList())).thenReturn(Arrays.asList(testRouteDto));
	    
	    // Act
	    List<RouteDto> result = routingService.getDelayedRoutes();
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(1, result.size());
	    assertEquals(testRouteDto.getId(), result.get(0).getId());
	    verify(routeRepository).findDelayedRoutes();
	}

	@Test
	void getAverageTimeByStep_Success() {
	    // Arrange
	    List<Object[]> timeData = Arrays.asList(
	            new Object[] {"INBOUND_DOCK", 5.0},
	            new Object[] {"SCANNER_STATION", 3.0}
	    );
	    when(routeRepository.findAverageEstimatedTimeByStep()).thenReturn(timeData);
	    
	    // Act
	    Map<String, Double> result = routingService.getAverageTimeByStep();
	    
	    // Assert
	    assertNotNull(result);
	    assertEquals(2, result.size());
	    assertEquals(5.0, result.get("INBOUND_DOCK"));
	    assertEquals(3.0, result.get("SCANNER_STATION"));
	    verify(routeRepository).findAverageEstimatedTimeByStep();
	}
}