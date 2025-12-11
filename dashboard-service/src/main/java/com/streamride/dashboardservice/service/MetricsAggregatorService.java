package com.streamride.dashboardservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamride.dashboardservice.model.CityMetrics;
import com.streamride.dashboardservice.model.DashboardMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MetricsAggregatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thread-safe storage for metrics
    private final Map<String, Long> cityActiveRides = new ConcurrentHashMap<>();
    private volatile Long globalActiveRides = 0L;
    private volatile Double averageDuration = 0.0;
    private volatile Instant lastUpdate = Instant.now();

    /**
     * Process incoming analytics message from Kafka
     */
    public void processAnalyticsMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.get("type").asText();

            switch (type) {
                case "TOP_ACTIVE_CITIES" -> processTopCities(jsonNode);
                case "CITY_ACTIVE_RIDES" -> processCityActiveRides(jsonNode);
                case "METRICS" -> processMetrics(jsonNode);
                case "ANOMALY" -> processAnomaly(jsonNode);
                default -> log.debug("Unknown message type: {}", type);
            }
            this.lastUpdate = Instant.now();
        } catch (Exception e) {
            log.error("Error processing analytics message: {}", message, e);
        }
    }

    private void processTopCities(JsonNode jsonNode) {
        try {
            cityActiveRides.clear();
            JsonNode cities = jsonNode.get("cities");
            if (cities.isArray()) {
                cities.forEach(city -> {
                    String cityName = city.get("city").asText();
                    Long count = city.get("count").asLong();
                    cityActiveRides.put(cityName, count);
                });
            }
        } catch (Exception e) {
            log.error("Error processing top cities", e);
        }
    }

    private void processCityActiveRides(JsonNode jsonNode) {
        try {
            String city = jsonNode.get("city").asText();
            Long activeRides = jsonNode.get("activeRides").asLong();
            cityActiveRides.put(city, activeRides);
            updateGlobalActiveRides();
        } catch (Exception e) {
            log.error("Error processing city active rides", e);
        }
    }

    private void processMetrics(JsonNode jsonNode) {
        try {
            this.averageDuration = jsonNode.get("avgDuration").asDouble();
        } catch (Exception e) {
            log.error("Error processing metrics", e);
        }
    }

    private void processAnomaly(JsonNode jsonNode) {
        try {
            String rideId = jsonNode.get("rideId").asText();
            Integer duration = jsonNode.get("duration").asInt();
            String city = jsonNode.get("city").asText();
            log.warn("ANOMALY DETECTED - City: {}, Ride ID: {}, Duration: {} minutes",
                    city, rideId, duration);
        } catch (Exception e) {
            log.error("Error processing anomaly", e);
        }
    }

    private void updateGlobalActiveRides() {
        this.globalActiveRides = cityActiveRides.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Get current dashboard metrics
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
     * Reset metrics (for testing)
     */
    public void reset() {
        cityActiveRides.clear();
        globalActiveRides = 0L;
        averageDuration = 0.0;
        lastUpdate = Instant.now();
    }
}
