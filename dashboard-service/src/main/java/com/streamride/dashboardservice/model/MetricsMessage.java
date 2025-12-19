package com.streamride.dashboardservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message containing aggregated metrics across all cities.
 * Includes active rides, completed rides, and average duration.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MetricsMessage extends AnalyticsMessage {
    private Long activeRides;
    private Long ridesCompleted;
    private Double avgDuration;
}
