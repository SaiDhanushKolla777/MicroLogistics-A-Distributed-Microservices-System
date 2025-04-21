package com.micrologistics.metrics.messaging.consumer;

import com.micrologistics.common.event.ItemRegisteredEvent;
import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Kafka consumer for item-registered events.
 * Updates throughput and item count metrics in response to new item registrations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemEventConsumer {

    private final MetricsService metricsService;

    /**
     * Consumes item registered events and records metrics.
     *
     * @param event Item registered event payload.
     */
    @KafkaListener(
            topics = "${kafka.topics.item-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "itemRegisteredKafkaListenerContainerFactory"
    )
    public void onItemRegistered(ItemRegisteredEvent event) {
        log.info("Consumed item registered event: id={}, trackingId={}", event.getId(), event.getTrackingId());

        // Throughput metric (counts as 1 for each item registered)
        OperationalMetrics throughputMetric = OperationalMetrics.builder()
                .metricName(OperationalMetrics.METRIC_THROUGHPUT)
                .serviceId(OperationalMetrics.SERVICE_ITEM_REGISTRATION)
                .resourceId("item-registration-service")
                .value(1.0)
                .unit(OperationalMetrics.UNIT_ITEMS_PER_SECOND)
                .timestamp(LocalDateTime.now())
                .build();
        metricsService.recordMetric(throughputMetric);

        // Item count metric (optional: could be real count, or just used for event tracking)
        OperationalMetrics itemCountMetric = OperationalMetrics.builder()
                .metricName(OperationalMetrics.METRIC_ITEM_COUNT)
                .serviceId(OperationalMetrics.SERVICE_ITEM_REGISTRATION)
                .resourceId(event.getId())
                .value(1.0)
                .unit(OperationalMetrics.UNIT_COUNT)
                .timestamp(LocalDateTime.now())
                .build();
        metricsService.recordMetric(itemCountMetric);

        // Optionally, other operational metrics can be captured here.
    }
}
