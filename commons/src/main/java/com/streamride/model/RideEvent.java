package com.streamride.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideEvent {
    private EventType eventType;
    private String rideId;
    private String driverId;
    private String riderId;
    private String city;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Double latitude;
    private Double longitude;
    private Double fareEstimate;
    private Integer durationMinutes;
    private Double distance;

    private static final String[] cities = {"Bangalore", "Hyderabad", "Mumbai", "Delhi"};
    private static final Random random = new Random();

    public static RideEvent random(String eventTypeStr, String city) {
        EventType eventType = EventType.valueOf(eventTypeStr.toUpperCase());

        return RideEvent.builder()
                .eventType(eventType)
                .rideId(UUID.randomUUID().toString())
                .driverId("DRIVER-" + random.nextInt(100))
                .riderId("RIDER-" + random.nextInt(1000))
                .city(city)
                .timestamp(LocalDateTime.now())
                .latitude(20 + random.nextDouble() * 15) // random approx latitude
                .longitude(75 + random.nextDouble() * 10) // random approx longitude
                .fareEstimate(10 + random.nextDouble() * 90)
                .durationMinutes(eventType == EventType.RIDE_COMPLETED ? random.nextInt(60) + 1 : null)
                .distance(eventType == EventType.RIDE_COMPLETED ? random.nextDouble() * 30 : null)
                .build();
    }

}