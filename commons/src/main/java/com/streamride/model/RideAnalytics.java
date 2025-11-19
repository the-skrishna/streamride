package com.streamride.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideAnalytics {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Long activeRides;
    private Double averageRideDuration;
    private Long totalRidesCompleted;
    private Map<String, Long> ridesByCity;
    private Long activeDrivers;
    private Long anomalousRides; // rides > threshold duration
}