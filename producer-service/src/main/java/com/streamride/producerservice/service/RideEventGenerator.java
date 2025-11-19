package com.streamride.producerservice.service;

import com.streamride.model.RideEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class RideEventGenerator {

    private final List<String> eventTypes = Arrays.asList(
            "ride_requested", "ride_started", "ride_completed", "driver_location_update"
    );

    public final List<String> cities = Arrays.asList(
            "Bangalore", "Hyderabad", "Mumbai", "Delhi"
    );

    public List<String> getCities() {
        return cities;
    }

    private final Random random = new Random();

    public RideEvent generateRandomEvent() {
        String eventType = eventTypes.get(random.nextInt(eventTypes.size()));
        String city = cities.get(random.nextInt(cities.size()));
        return RideEvent.random(eventType, city);
    }
}
