package com.micrologistics.routing.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for item registration events.
 * Processes new items that need routing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemRegistrationConsumer {
    
    private final RoutingService routingService;
    
    /**
     * Consume item registered events from Kafka.
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
            routingService.processItemRegisteredEvent(event);
            log.info("Successfully processed item registered event for itemId={}", event.getId());
        } catch (Exception e) {
            log.error("Error processing item registered event for itemId={}: {}", 
                    event.getId(), e.getMessage(), e);
            // In a production environment, we'd implement error handling, retries,
            // and a dead-letter queue for failed messages
        }
    }
}
