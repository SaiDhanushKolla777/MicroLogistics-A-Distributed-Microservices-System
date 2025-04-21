package com.micrologistics.container.messaging.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.micrologistics.common.event.ContainerStatusEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for container-related events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerEventPublisher {
    
    private final KafkaTemplate<String, ContainerStatusEvent> kafkaTemplate;
    
    @Value("${kafka.topics.container-status}")
    private String containerStatusTopic;
    
    /**
     * Publish a container status event.
     * 
     * @param event The container status event
     */
    public void publishContainerStatusEvent(ContainerStatusEvent event) {
        try {
            kafkaTemplate.send(containerStatusTopic, event.getId(), event);
            log.info("Published container status event for container ID: {}, status: {}", 
                    event.getId(), event.getStatus());
        } catch (Exception e) {
            log.error("Error publishing container status event for container ID: {}", event.getId(), e);
            // In a production environment, would use a retry mechanism or dead letter queue
        }
    }
}
