package com.streamride.dashboardservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message containing active ride count for a specific city.
 * Sent whenever a city's active ride count changes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CityActiveRidesMessage extends AnalyticsMessage {
    private String city;
    private Long activeRides;
}
