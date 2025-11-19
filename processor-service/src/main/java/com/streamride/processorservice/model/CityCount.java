package com.streamride.processorservice.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CityCount {
    private String city;
    private long count;

    public CityCount() {} // Default constructor for JSON Serde

    public CityCount(String city, long count) {
        this.city = city;
        this.count = count;
    }

}
