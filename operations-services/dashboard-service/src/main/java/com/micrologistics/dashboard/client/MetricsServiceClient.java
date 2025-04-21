package com.micrologistics.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "metrics-service")
public interface MetricsServiceClient {
    @GetMapping("/api/metrics/health")
    Map<String, Object> getSystemHealthMetrics();

    @GetMapping("/api/metrics/throughput/report")
    Map<String, Object> getThroughputReport(@RequestParam("startTime") String start,
                                            @RequestParam("endTime") String end);
}
