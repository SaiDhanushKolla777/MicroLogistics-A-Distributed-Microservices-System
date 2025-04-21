package com.micrologistics.metrics.service;

import com.micrologistics.metrics.entity.OperationalMetrics;
import com.micrologistics.metrics.repository.OperationalMetricsRepository;
import com.micrologistics.metrics.repository.TimeSeriesRepository;
import com.micrologistics.metrics.service.impl.MetricsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricsServiceTest {

    @Mock
    private OperationalMetricsRepository metricsRepository;
    @Mock
    private TimeSeriesRepository timeSeriesRepository;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void recordMetric_Success() {
        OperationalMetrics metrics = OperationalMetrics.builder()
                .metricName("test_metric")
                .serviceId("test-service")
                .value(100.0)
                .unit("ms")
                .timestamp(LocalDateTime.now())
                .build();

        when(metricsRepository.save(any(OperationalMetrics.class))).thenReturn(metrics);
        when(timeSeriesRepository.save(any())).thenReturn(true);

        OperationalMetrics result = metricsService.recordMetric(metrics);
        assertNotNull(result);
        assertEquals(metrics.getMetricName(), result.getMetricName());
    }

    @Test
    void getMetricsByName_ReturnsList() {
        List<OperationalMetrics> list = Collections.singletonList(
                OperationalMetrics.builder().metricName("test").build());
        when(metricsRepository.findByMetricNameAndTimestampBetween(any(), any(), any()))
                .thenReturn(list);
        List<OperationalMetrics> result = metricsService.getMetricsByName("test", LocalDateTime.now(), LocalDateTime.now());
        assertFalse(result.isEmpty());
    }
}
