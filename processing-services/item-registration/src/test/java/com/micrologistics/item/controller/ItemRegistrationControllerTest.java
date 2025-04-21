package com.micrologistics.item.controller;

import static org.mockito.ArgumentMatchers.;
import static org.mockito.Mockito.;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.item.entity.Item;
import com.micrologistics.item.service.ItemRegistrationService;

@WebMvcTest(ItemRegistrationController.class)
class ItemRegistrationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ItemRegistrationService itemService;

	private ObjectMapper objectMapper;
	private ItemDto testItemDto;

	@BeforeEach
	void setUp() {
	    objectMapper = new ObjectMapper();
	    objectMapper.registerModule(new JavaTimeModule());
	    
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
	void registerItem_Success() throws Exception {
	    // Arrange
	    when(itemService.registerItem(any(ItemDto.class))).thenReturn(testItemDto);
	    
	    // Act & Assert
	    mockMvc.perform(post("/api/items")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(testItemDto)))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()))
	            .andExpect(jsonPath("$.description").value(testItemDto.getDescription()));
	    
	    verify(itemService).registerItem(any(ItemDto.class));
	}

	@Test
	void getItemById_Success() throws Exception {
	    // Arrange
	    when(itemService.getItemById(anyString())).thenReturn(testItemDto);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items/{id}", "1"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()))
	            .andExpect(jsonPath("$.trackingId").value(testItemDto.getTrackingId()));
	    
	    verify(itemService).getItemById("1");
	}

	@Test
	void getItemByTrackingId_Success() throws Exception {
	    // Arrange
	    when(itemService.getItemByTrackingId(anyString())).thenReturn(testItemDto);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items/tracking/{trackingId}", "TRK-12345678"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()))
	            .andExpect(jsonPath("$.trackingId").value(testItemDto.getTrackingId()));
	    
	    verify(itemService).getItemByTrackingId("TRK-12345678");
	}

	@Test
	void getItems_Success() throws Exception {
	    // Arrange
	    Page<ItemDto> itemsPage = new PageImpl<>(Arrays.asList(testItemDto));
	    when(itemService.getAllItems(any(Pageable.class))).thenReturn(itemsPage);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items")
	            .param("page", "0")
	            .param("size", "10"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.content.id").value(testItemDto.getId()));
	    
	    verify(itemService).getAllItems(any(Pageable.class));
	}

	@Test
	void getItemsByStatus_Success() throws Exception {
	    // Arrange
	    Page<ItemDto> itemsPage = new PageImpl<>(Arrays.asList(testItemDto));
	    when(itemService.getItemsByStatus(anyString(), any(Pageable.class))).thenReturn(itemsPage);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items")
	            .param("page", "0")
	            .param("size", "10")
	            .param("status", Item.STATUS_REGISTERED))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.content.id").value(testItemDto.getId()));
	    
	    verify(itemService).getItemsByStatus(eq(Item.STATUS_REGISTERED), any(Pageable.class));
	}

	@Test
	void getItemsByDestination_Success() throws Exception {
	    // Arrange
	    List<ItemDto> items = Arrays.asList(testItemDto);
	    when(itemService.getItemsByDestination(anyString())).thenReturn(items);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items/destination/{destination}", "New York"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()))
	            .andExpect(jsonPath("$.destination").value(testItemDto.getDestination()));
	    
	    verify(itemService).getItemsByDestination("New York");
	}

	@Test
	void updateItem_Success() throws Exception {
	    // Arrange
	    when(itemService.updateItem(anyString(), any(ItemDto.class))).thenReturn(testItemDto);
	    
	    // Act & Assert
	    mockMvc.perform(put("/api/items/{id}", "1")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(testItemDto)))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()));
	    
	    verify(itemService).updateItem(eq("1"), any(ItemDto.class));
	}

	@Test
	void updateItemStatus_Success() throws Exception {
	    // Arrange
	    when(itemService.updateItemStatus(anyString(), anyString())).thenReturn(testItemDto);
	    
	    // Act & Assert
	    mockMvc.perform(put("/api/items/{id}/status", "1")
	            .param("status", Item.STATUS_ROUTING))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id").value(testItemDto.getId()));
	    
	    verify(itemService).updateItemStatus("1", Item.STATUS_ROUTING);
	}

	@Test
	void deleteItem_Success() throws Exception {
	    // Arrange
	    doNothing().when(itemService).deleteItem(anyString());
	    
	    // Act & Assert
	    mockMvc.perform(delete("/api/items/{id}", "1"))
	            .andExpect(status().isNoContent());
	    
	    verify(itemService).deleteItem("1");
	}

	@Test
	void getItemCountsByStatus_Success() throws Exception {
	    // Arrange
	    Map<String, Long> counts = new HashMap<>();
	    counts.put(Item.STATUS_REGISTERED, 10L);
	    counts.put(Item.STATUS_ROUTING, 5L);
	    when(itemService.getItemCountsByStatus()).thenReturn(counts);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items/stats/counts"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.REGISTERED").value(10))
	            .andExpect(jsonPath("$.ROUTING").value(5));
	    
	    verify(itemService).getItemCountsByStatus();
	}

	@Test
	void getAverageWeightByDestination_Success() throws Exception {
	    // Arrange
	    Map<String, Double> averageWeights = new HashMap<>();
	    averageWeights.put("New York", 12.5);
	    averageWeights.put("Los Angeles", 15.2);
	    when(itemService.getAverageWeightByDestination()).thenReturn(averageWeights);
	    
	    // Act & Assert
	    mockMvc.perform(get("/api/items/stats/weight"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.['New York']").value(12.5))
	            .andExpect(jsonPath("$.['Los Angeles']").value(15.2));
	    
	    verify(itemService).getAverageWeightByDestination();
	}

}