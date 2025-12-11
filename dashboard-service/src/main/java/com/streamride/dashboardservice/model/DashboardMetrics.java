package com.streamride.dashboardservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    private Long activeRides;
    private Double averageDuration;
    private List<CityMetrics> topCities;

    private Instant timestamp;
}
