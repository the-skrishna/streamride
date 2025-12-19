package com.streamride.processorservice.pipeline;

import com.streamride.processorservice.mapper.MetricsJsonMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

/**
 * Pipeline for publishing per-city active ride counts.
 * 
 * Emits CITY_ACTIVE_RIDES messages whenever a city's active ride count changes.
 */
@Component
public class CityActiveRidesPipeline {

    private static final String OUTPUT_TOPIC = "rides.analytics";

    private final MetricsJsonMapper jsonMapper;

    public CityActiveRidesPipeline(MetricsJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Builds and executes the city active rides pipeline.
     * 
     * @param activeRides KTable of active rides per city
     */
    public void build(KTable<String, Long> activeRides) {
        activeRides.toStream()
                .map((city, count) -> KeyValue.pair(city, jsonMapper.createCityActiveRidesJson(city, count)))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
}
