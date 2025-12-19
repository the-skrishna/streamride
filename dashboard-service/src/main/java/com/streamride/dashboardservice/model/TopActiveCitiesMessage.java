package com.streamride.dashboardservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Message containing the top active cities by ride count.
 * Sent periodically by the processor-service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TopActiveCitiesMessage extends AnalyticsMessage {
    private List<CityCount> cities;
}
