package com.micrologistics.metrics.analyzer;

import com.micrologistics.metrics.entity.OperationalMetrics;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BottleneckDetectorTest {

    @Test
    void findWorstLatencyService_ReturnsCorrectService() {
        List<OperationalMetrics> metrics = Arrays.asList(
                OperationalMetrics.builder().metricName("latency").serviceId("svc1").value(100.0).timestamp(LocalDateTime.now()).build(),
                OperationalMetrics.builder().metricName("latency").serviceId("svc2").value(250.0).timestamp(LocalDateTime.now()).build()
        );
        BottleneckDetector detector = new BottleneckDetector();
        String result = detector.findWorstLatencyService(metrics);
        assertEquals("svc2", result);
    }

    @Test
    void isBottleneck_DetectsHighLatency() {
        List<OperationalMetrics> metrics = Arrays.asList(
                OperationalMetrics.builder().metricName("latency").serviceId("svc1").value(99.0).timestamp(LocalDateTime.now()).build(),
                OperationalMetrics.builder().metricName("latency").serviceId("svc1").value(500.0).timestamp(LocalDateTime.now()).build()
        );
        BottleneckDetector detector = new BottleneckDetector();
        assertTrue(detector.isBottleneck(metrics, 300.0));
        assertFalse(detector.isBottleneck(metrics, 600.0));
    }
}
