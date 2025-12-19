package com.streamride.dashboardservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a city and its associated count.
 * Used in TOP_ACTIVE_CITIES messages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityCount {
    private String city;
    private Long count;
}
