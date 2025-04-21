package com.micrologistics.metrics.messaging.consumer;

import com.micrologistics.common.event.ContainerStatusEvent;
import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumes container status events and updates container-related metrics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerEventConsumer {

    private final MetricsService metricsService;

    @KafkaListener(
            topics = "${kafka.topics.container-status:container-status}",
            groupId = "${spring.kafka.consumer.group-id:container-management-group}",
            containerFactory = "itemRegisteredKafkaListenerContainerFactory"
    )
    public void onContainerStatusEvent(ContainerStatusEvent event) {
        log.info("Consumed container status event: containerId={}, status={}", event.getId(), event.getStatus());

        // Example: Register a throughput or status metric for containers
        OperationalMetrics containerCountMetric = OperationalMetrics.builder()
                .metricName(OperationalMetrics.METRIC_CONTAINER_COUNT)
                .serviceId(OperationalMetrics.SERVICE_CONTAINER)
                .resourceId(event.getId())
                .value(1.0)
                .unit(OperationalMetrics.UNIT_COUNT)
                .timestamp(LocalDateTime.now())
                .build();
        metricsService.recordMetric(containerCountMetric);

        // Example: Utilization metric (if available)
        if (event.getCurrentWeight() != null && event.getMaxWeight() != null && event.getMaxWeight() > 0) {
            double utilization = (event.getCurrentWeight() / event.getMaxWeight()) * 100.0;
            OperationalMetrics utilizationMetric = OperationalMetrics.builder()
                    .metricName(OperationalMetrics.METRIC_UTILIZATION)
                    .serviceId(OperationalMetrics.SERVICE_CONTAINER)
                    .resourceId(event.getId())
                    .value(utilization)
                    .unit(OperationalMetrics.UNIT_PERCENTAGE)
                    .timestamp(LocalDateTime.now())
                    .build();
            metricsService.recordMetric(utilizationMetric);
        }
    }
}
