package com.streamride.processorservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.streamride.model.RideEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JSON mapper for anomaly detection messages.
 * 
 * Creates JSON messages for rides that exceed the duration threshold.
 */
@Component
@Slf4j
public class AnomalyJsonMapper {

    private final ObjectMapper objectMapper;

    public AnomalyJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates an ANOMALY JSON message.
     * 
     * @param event ride event that triggered the anomaly
     * @return JSON string for ANOMALY message
     */
    public String createAnomalyJson(RideEvent event) {
        try {
            ObjectNode anomaly = objectMapper.createObjectNode();
            anomaly.put("type", "ANOMALY");
            anomaly.put("city", event.getCity());
            anomaly.put("rideId", event.getRideId());
            anomaly.put("duration", event.getDurationMinutes());
            anomaly.put("message", "Long ride detected");
            anomaly.put("timestamp", System.currentTimeMillis());
            return anomaly.toString();
        } catch (Exception e) {
            log.error("Error creating ANOMALY JSON", e);
            return "";
        }
    }
}
