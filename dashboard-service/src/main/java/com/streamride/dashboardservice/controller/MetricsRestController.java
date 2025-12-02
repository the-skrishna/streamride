package com.streamride.dashboardservice.controller;

import com.streamride.dashboardservice.model.DashboardMetrics;
import com.streamride.dashboardservice.service.MetricsAggregatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MetricsRestController {

    private final MetricsAggregatorService metricsAggregator;

    @Autowired
    public MetricsRestController(MetricsAggregatorService metricsAggregator) {
        this.metricsAggregator = metricsAggregator;
    }

    /**
     * Get current metrics (for polling fallback)
     */
    @GetMapping("/metrics/current")
    public ResponseEntity<DashboardMetrics> getCurrentMetrics() {
        DashboardMetrics metrics = metricsAggregator.getCurrentMetrics();
        log.debug("REST API - returning metrics: {}", metrics);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Dashboard Service is running");
    }
}
