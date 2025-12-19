package com.streamride.dashboardservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message indicating an anomaly has been detected.
 * Currently used for rides exceeding the duration threshold.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnomalyMessage extends AnalyticsMessage {
    private String city;
    private String rideId;
    private Integer duration;
    private String message;
}
