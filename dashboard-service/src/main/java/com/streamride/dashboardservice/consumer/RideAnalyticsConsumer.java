package com.streamride.dashboardservice.consumer;

import com.streamride.dashboardservice.service.MetricsAggregatorService;
import com.streamride.model.config.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RideAnalyticsConsumer {

    private final MetricsAggregatorService metricsAggregator;

    @Autowired
    public RideAnalyticsConsumer(MetricsAggregatorService metricsAggregator) {
        this.metricsAggregator = metricsAggregator;
    }

    @KafkaListener(topics = KafkaTopics.RIDES_ANALYTICS, groupId = "dashboard-service")
    public void consume(String message) {
        log.debug("Received message from rides.analytics: {}", message);
        metricsAggregator.processAnalyticsMessage(message);
    }
}
