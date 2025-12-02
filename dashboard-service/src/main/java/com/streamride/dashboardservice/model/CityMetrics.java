package com.streamride.dashboardservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityMetrics {

    private String city;
    private Long activeRides;

    public long getActiveRides() {
        return activeRides != null ? activeRides : 0L;
    }
}
