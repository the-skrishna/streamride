package com.streamride.processorservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.streamride.processorservice.model.CityCount;
import com.streamride.processorservice.model.CityMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON mapper for metrics-related analytics messages.
 * 
 * Centralizes JSON creation logic to eliminate code duplication
 * and provide consistent message formatting.
 */
@Component
@Slf4j
public class MetricsJsonMapper {

    private final ObjectMapper objectMapper;

    public MetricsJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a METRICS JSON message.
     * 
     * @param cityMetrics city metrics containing active and completed ride counts
     * @param avgDuration average ride duration
     * @return JSON string for METRICS message
     */
    public String createMetricsJson(CityMetrics cityMetrics, Double avgDuration) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("type", "METRICS");
            json.put("activeRides", cityMetrics.getActiveRides());
            json.put("ridesCompleted", cityMetrics.getRidesCompleted());
            json.put("avgDuration", avgDuration == null ? 0.0 : avgDuration);
            json.put("timestamp", System.currentTimeMillis());
            return json.toString();
        } catch (Exception e) {
            log.error("Error creating METRICS JSON", e);
            return "{}";
        }
    }

    /**
     * Creates a CITY_ACTIVE_RIDES JSON message.
     * 
     * @param city  city name
     * @param count active ride count
     * @return JSON string for CITY_ACTIVE_RIDES message
     */
    public String createCityActiveRidesJson(String city, Long count) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("type", "CITY_ACTIVE_RIDES");
            json.put("city", city);
            json.put("activeRides", count);
            json.put("timestamp", System.currentTimeMillis());
            return json.toString();
        } catch (Exception e) {
            log.error("Error creating CITY_ACTIVE_RIDES JSON", e);
            return "{}";
        }
    }

    /**
     * Creates a TOP_ACTIVE_CITIES JSON message.
     * 
     * @param cityMap map of city names to active ride counts
     * @return JSON string for TOP_ACTIVE_CITIES message
     */
    public String createTopCitiesJson(Map<String, Long> cityMap) {
        try {
            // Sort and limit to top 5 cities
            List<CityCount> top5 = cityMap.entrySet().stream()
                    .map(e -> new CityCount(e.getKey(), e.getValue()))
                    .sorted((c1, c2) -> Long.compare(c2.getCount(), c1.getCount()))
                    .limit(5)
                    .collect(Collectors.toList());

            ObjectNode json = objectMapper.createObjectNode();
            json.put("type", "TOP_ACTIVE_CITIES");
            json.putPOJO("cities", top5);
            json.put("timestamp", System.currentTimeMillis());
            return json.toString();
        } catch (Exception e) {
            log.error("Error creating TOP_ACTIVE_CITIES JSON", e);
            return "{}";
        }
    }
}
