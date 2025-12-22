package com.streamride.dashboardservice.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * Base class for all analytics messages received from Kafka.
 * Uses Jackson polymorphic deserialization to automatically
 * deserialize to the correct subtype based on the "type" field.
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TopActiveCitiesMessage.class, name = "TOP_ACTIVE_CITIES"),
        @JsonSubTypes.Type(value = CityActiveRidesMessage.class, name = "CITY_ACTIVE_RIDES"),
        @JsonSubTypes.Type(value = MetricsMessage.class, name = "METRICS"),
        @JsonSubTypes.Type(value = AnomalyMessage.class, name = "ANOMALY")
})
public abstract class AnalyticsMessage {
    private String type;
    private Long timestamp;
}
