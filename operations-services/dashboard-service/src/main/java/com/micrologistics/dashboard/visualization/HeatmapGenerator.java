package com.micrologistics.dashboard.visualization;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class HeatmapGenerator {

    /**
     * Generates a mock heatmap representing item flow per zone.
     */
    public Map<String, Integer> generate() {
        Map<String, Integer> heatmap = new HashMap<>();
        heatmap.put("ZONE_A", 120);
        heatmap.put("ZONE_B", 75);
        heatmap.put("ZONE_C", 200);
        return heatmap;
    }
}
