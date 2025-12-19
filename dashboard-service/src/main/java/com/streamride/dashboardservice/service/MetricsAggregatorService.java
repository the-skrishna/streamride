package com.streamride.dashboardservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamride.dashboardservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for aggregating analytics metrics from Kafka messages.
 * Uses strongly-typed POJOs for message deserialization to separate
 * deserialization logic from business logic.
 */
@Service
@Slf4j
public class MetricsAggregatorService {

    private final ObjectMapper objectMapper;

    // Thread-safe storage for metrics
    private final Map<String, Long> cityActiveRides = new ConcurrentHashMap<>();
    private volatile Long globalActiveRides = 0L;
    private volatile Double averageDuration = 0.0;
    private volatile Instant lastUpdate = Instant.now();

    /**
     * Constructor with ObjectMapper injection.
     * Uses the shared singleton ObjectMapper bean to avoid redundant
     * instantiations.
     *
     * @param objectMapper shared ObjectMapper instance
     */
    public MetricsAggregatorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Process incoming analytics message from Kafka.
     * Deserializes the message to the appropriate POJO type and delegates
     * to specific processing methods.
     *
     * @param message JSON string containing analytics data
     */
    public void processAnalyticsMessage(String message) {
        try {
            // Deserialize to base class - Jackson automatically determines the correct
            // subtype
            AnalyticsMessage analyticsMessage = objectMapper.readValue(message, AnalyticsMessage.class);

            // Delegate to specific processing methods based on message type
            if (analyticsMessage instanceof TopActiveCitiesMessage msg) {
                processTopCities(msg);
            } else if (analyticsMessage instanceof CityActiveRidesMessage msg) {
                processCityActiveRides(msg);
            } else if (analyticsMessage instanceof MetricsMessage msg) {
                processMetrics(msg);
            } else if (analyticsMessage instanceof AnomalyMessage msg) {
                processAnomaly(msg);
            } else {
                log.debug("Unknown message type: {}", analyticsMessage.getType());
            }

            this.lastUpdate = Instant.now();
        } catch (Exception e) {
            log.error("Error processing analytics message: {}", message, e);
        }
    }

    /**
     * Process top active cities message.
     * Updates the city active rides map with the top cities data.
     *
     * @param message TOP_ACTIVE_CITIES message
     */
    private void processTopCities(TopActiveCitiesMessage message) {
        try {
            cityActiveRides.clear();
            if (message.getCities() != null) {
                message.getCities()
                        .forEach(cityCount -> cityActiveRides.put(cityCount.getCity(), cityCount.getCount()));
            }
            log.debug("Processed top cities: {} cities", cityActiveRides.size());
        } catch (Exception e) {
            log.error("Error processing top cities", e);
        }
    }

    /**
     * Process city active rides message.
     * Updates the active rides count for a specific city.
     *
     * @param message CITY_ACTIVE_RIDES message
     */
    private void processCityActiveRides(CityActiveRidesMessage message) {
        try {
            cityActiveRides.put(message.getCity(), message.getActiveRides());
            updateGlobalActiveRides();
            log.debug("Updated active rides for {}: {}", message.getCity(), message.getActiveRides());
        } catch (Exception e) {
            log.error("Error processing city active rides", e);
        }
    }

    /**
     * Process metrics message.
     * Updates the global average duration metric.
     *
     * @param message METRICS message
     */
    private void processMetrics(MetricsMessage message) {
        try {
            this.averageDuration = message.getAvgDuration();
            log.debug("Updated average duration: {}", averageDuration);
        } catch (Exception e) {
            log.error("Error processing metrics", e);
        }
    }

    /**
     * Process anomaly message.
     * Logs anomaly warnings for monitoring purposes.
     *
     * @param message ANOMALY message
     */
    private void processAnomaly(AnomalyMessage message) {
        try {
            log.warn("ANOMALY DETECTED - City: {}, Ride ID: {}, Duration: {} minutes",
                    message.getCity(), message.getRideId(), message.getDuration());
        } catch (Exception e) {
            log.error("Error processing anomaly", e);
        }
    }

    /**
     * Update global active rides count by summing all city counts.
     */
    private void updateGlobalActiveRides() {
        this.globalActiveRides = cityActiveRides.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Get current dashboard metrics.
     * Returns aggregated metrics for the dashboard UI.
     *
     * @return current dashboard metrics
     */
    public DashboardMetrics getCurrentMetrics() {
        List<CityMetrics> topCities = cityActiveRides.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(e -> CityMetrics.builder()
                        .city(e.getKey())
                        .activeRides(e.getValue())
                        .build())
                .toList();

        return DashboardMetrics.builder()
                .activeRides(globalActiveRides)
                .averageDuration(averageDuration)
                .topCities(topCities)
                .timestamp(lastUpdate)
                .build();
    }

    /**
     * Reset metrics (for testing).
     */
    public void reset() {
        cityActiveRides.clear();
        globalActiveRides = 0L;
        averageDuration = 0.0;
        lastUpdate = Instant.now();
    }
}
