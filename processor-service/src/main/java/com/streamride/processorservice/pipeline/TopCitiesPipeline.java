package com.streamride.processorservice.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.streamride.processorservice.model.CityCount;
import com.streamride.processorservice.mapper.MetricsJsonMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline for publishing top 5 cities by active ride count.
 * 
 * Maintains an aggregated state of all cities and emits TOP_ACTIVE_CITIES
 * messages
 * with the top 5 cities sorted by active ride count.
 */
@Component
public class TopCitiesPipeline {

    private static final String OUTPUT_TOPIC = "rides.analytics";
    private static final String TOP_CITIES_STORE = "top-cities-store";

    private final MetricsJsonMapper jsonMapper;

    public TopCitiesPipeline(MetricsJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Builds and executes the top cities pipeline.
     * 
     * @param activeRides KTable of active rides per city
     */
    public void build(KTable<String, Long> activeRides) {
        // Convert to CityCount stream with a common key for aggregation
        KStream<String, CityCount> cityCountStream = activeRides.toStream()
                .map((city, count) -> KeyValue.pair("TOP_CITIES_KEY", new CityCount(city, count)));

        // Aggregate all cities into a single map
        KTable<String, Map<String, Long>> topCitiesAggregation = cityCountStream
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(CityCount.class)))
                .aggregate(
                        HashMap::new,
                        (aggKey, cityCount, state) -> {
                            // Remove cities with zero active rides
                            if (cityCount.getCount() == 0) {
                                state.remove(cityCount.getCity());
                            } else {
                                state.put(cityCount.getCity(), cityCount.getCount());
                            }
                            return state;
                        },
                        Materialized.<String, Map<String, Long>, KeyValueStore<Bytes, byte[]>>as(TOP_CITIES_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(new TypeReference<Map<String, Long>>() {
                                })));

        // Convert map to sorted top 5 list and publish
        topCitiesAggregation.toStream()
                .mapValues(jsonMapper::createTopCitiesJson)
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
}
