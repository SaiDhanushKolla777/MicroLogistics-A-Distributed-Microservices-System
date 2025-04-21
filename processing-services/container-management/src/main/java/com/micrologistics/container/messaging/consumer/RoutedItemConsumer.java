package com.micrologistics.container.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.micrologistics.common.dto.ItemDto;
import com.micrologistics.common.dto.RouteDto;
import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.container.service.ContainerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for routed item events.
 * Processes new items that need to be placed in containers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoutedItemConsumer {
    
    private final ContainerService containerService;
    
    /**
     * Consume item registered events from Kafka.
     * Note: In a real system, you would likely have a dedicated event for routed items,
     * but for this example, we're using the ItemRegisteredEvent.
     * 
     * @param event The item registered event
     */
    @KafkaListener(
        topics = "${kafka.topics.item-registered}",
        containerFactory = "itemRegisteredKafkaListenerContainerFactory"
    )
    public void consumeItemRegisteredEvent(ItemRegisteredEvent event) {
        log.info("Received item registered event: itemId={}, trackingId={}", 
                event.getId(), event.getTrackingId());
        
        try {
            // Convert event to an ItemDto
            ItemDto itemDto = ItemDto.builder()
                    .id(event.getId())
                    .trackingId(event.getTrackingId())
                    .description(event.getDescription())
                    .weight(event.getWeight())
                    .length(event.getLength())
                    .width(event.getWidth())
                    .height(event.getHeight())
                    .destination(event.getDestination())
                    .build();
            
            // Find optimal container for the item
            log.info("Finding optimal container for item: {}", itemDto.getId());
            var containerDto = containerService.findOptimalContainerForItem(itemDto);
            
            if (containerDto != null) {
                // Add the item to the container
                log.info("Adding item {} to container {}", itemDto.getId(), containerDto.getId());
                containerService.addItemToContainer(containerDto.getId(), itemDto);
            } else {
                log.info("No suitable container found for item {}", itemDto.getId());
                // In a real system, you might create a new container or put the item in a queue
            }
            
        } catch (Exception e) {
            log.error("Error processing item registered event for itemId={}: {}", 
                    event.getId(), e.getMessage(), e);
            // In a production environment, we'd implement error handling, retries,
            // and a dead-letter queue for failed messages
        }
    }
}
