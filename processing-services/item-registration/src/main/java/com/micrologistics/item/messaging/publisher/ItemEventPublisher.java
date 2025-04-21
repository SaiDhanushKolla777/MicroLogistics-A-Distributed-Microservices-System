package com.micrologistics.item.messaging.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.item.entity.Item;
import com.micrologistics.item.mapper.ItemMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for item-related events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemEventPublisher {
    
    private final KafkaTemplate<String, ItemRegisteredEvent> kafkaTemplate;
    private final ItemMapper itemMapper;
    
    @Value("${kafka.topics.item-registered}")
    private String itemRegisteredTopic;
    
    /**
     * Publish an item registered event.
     * 
     * @param item The item that was registered
     */
    public void publishItemRegisteredEvent(Item item) {
        try {
            ItemRegisteredEvent event = itemMapper.toEvent(item);
            kafkaTemplate.send(itemRegisteredTopic, item.getId(), event);
            log.info("Published item registered event for item ID: {}", item.getId());
        } catch (Exception e) {
            log.error("Error publishing item registered event for item ID: {}", item.getId(), e);
            // In a production environment, would use a retry mechanism or dead letter queue
        }
    }
}
