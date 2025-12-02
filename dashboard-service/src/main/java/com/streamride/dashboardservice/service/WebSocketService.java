package com.streamride.dashboardservice.service;

import com.streamride.dashboardservice.model.DashboardMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsAggregatorService metricsAggregator;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate,
                            MetricsAggregatorService metricsAggregator) {
        this.messagingTemplate = messagingTemplate;
        this.metricsAggregator = metricsAggregator;
    }

    /**
     * Broadcast metrics to all connected WebSocket clients every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastMetrics() {
        try {
            DashboardMetrics metrics = metricsAggregator.getCurrentMetrics();
            messagingTemplate.convertAndSend("/topic/metrics", metrics);
            log.debug("Broadcasted metrics to WebSocket clients: {}", metrics);
        } catch (Exception e) {
            log.error("Error broadcasting metrics", e);
        }
    }
}
