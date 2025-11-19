package com.streamride.processorservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityMetrics {
    private long activeRides;
    private long ridesCompleted;
}
