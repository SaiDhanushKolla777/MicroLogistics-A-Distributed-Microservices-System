package com.micrologistics.metrics.messaging.consumer;

import com.micrologistics.common.event.RouteEvent;
import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumes route events and updates routing-related metrics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RouteEventConsumer {

    private final MetricsService metricsService;

    @KafkaListener(
            topics = "${kafka.topics.route-events:route-events}",
            groupId = "${spring.kafka.consumer.group-id:routing-service-group}",
            containerFactory = "itemRegisteredKafkaListenerContainerFactory"
    )
    public void onRouteEvent(RouteEvent event) {
        log.info("Consumed route event: routeId={}", event.getRouteId());

        // Example: Register a throughput metric for each new route
        OperationalMetrics throughputMetric = OperationalMetrics.builder()
                .metricName(OperationalMetrics.METRIC_THROUGHPUT)
                .serviceId(OperationalMetrics.SERVICE_ROUTING)
                .resourceId(event.getRouteId())
                .value(1.0)
                .unit(OperationalMetrics.UNIT_COUNT)
                .timestamp(LocalDateTime.now())
                .build();
        metricsService.recordMetric(throughputMetric);

        // Optionally, more route-related metrics can be recorded here.
    }
}
