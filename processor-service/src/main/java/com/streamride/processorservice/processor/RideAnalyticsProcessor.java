package com.streamride.processorservice.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.streamride.model.EventType;
import com.streamride.model.RideEvent;
import com.streamride.processorservice.model.CityCount;
import com.streamride.processorservice.model.CityMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RideAnalyticsProcessor {

    private static final String SOURCE_TOPIC = "rides.events";
    private static final String OUTPUT_TOPIC = "rides.analytics";
    private static final int LONG_RIDE_THRESHOLD_MINUTES = 60;

    @Autowired
    public void process(StreamsBuilder builder) {

        ObjectMapper mapper = new ObjectMapper();

        JsonSerde<RideEvent> rideEventSerde = new JsonSerde<>(RideEvent.class);
        Serde<String> stringSerde = Serdes.String();
        Serde<Long> longSerde = Serdes.Long();
        Serde<Double> doubleSerde = Serdes.Double();

        // -------------- SOURCE STREAM ----------------
        KStream<String, RideEvent> events = builder.stream(SOURCE_TOPIC,
                Consumed.with(stringSerde, rideEventSerde));

        // -------------- METRICS ----------------

        //  Active rides (increment on STARTED, decrement on COMPLETED)
        KTable<String, Long> activeRides = events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_STARTED ||
                        v.getEventType() == EventType.RIDE_COMPLETED)
                .groupBy((k, v) -> v.getCity(), Grouped.with(stringSerde, rideEventSerde))
                .aggregate(
                        () -> 0L,
                        (city, event, currentCount) -> {
                            if (event.getEventType() == EventType.RIDE_STARTED) {
                                return currentCount + 1;  // Increment when ride starts
                            } else if (event.getEventType() == EventType.RIDE_COMPLETED) {
                                return Math.max(0, currentCount - 1);  // Decrement when ride completes
                            }
                            return currentCount;
                        },
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("active-rides-store")
                                .withKeySerde(stringSerde)
                                .withValueSerde(longSerde)
                );

        // Completed rides per city
        KTable<String, Long> ridesCompleted = events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED)
                .groupBy((k, v) -> v.getCity(), Grouped.with(stringSerde, rideEventSerde))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("rides-count-store")
                        .withKeySerde(stringSerde)
                        .withValueSerde(longSerde));

        // Total duration
        KTable<String, Long> totalDuration = events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED &&
                        v.getDurationMinutes() != null)
                .groupBy((k, v) -> v.getCity(), Grouped.with(stringSerde, rideEventSerde))
                .aggregate(
                        () -> 0L,
                        (city, event, total) -> total + event.getDurationMinutes(),
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                                        "total-duration-store")
                                .withKeySerde(stringSerde)
                                .withValueSerde(longSerde));

        // Average duration = total / count
        KTable<String, Double> avgDuration = totalDuration.join(
                ridesCompleted,
                (total, count) -> count == 0 ? 0.0 : (double) total / count,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("avg-duration-store")
                        .withKeySerde(stringSerde)
                        .withValueSerde(doubleSerde));

        // Combine activeRides and ridesCompleted into CityMetrics KStream
        KStream<String, CityMetrics> combined = activeRides
                .toStream()
                .leftJoin(ridesCompleted,
                        (active, completed) -> new CityMetrics(
                                active == null ? 0 : active,
                                completed == null ? 0 : completed));

        // Join combined KStream with avgDuration KTable to create final JSON
        KStream<String, String> metrics = combined.join(
                avgDuration,
                (cityMetrics, avg) -> {
                    try {
                        ObjectNode json = mapper.createObjectNode();
                        json.put("type", "METRICS");
                        json.put("activeRides", cityMetrics.getActiveRides());
                        json.put("ridesCompleted", cityMetrics.getRidesCompleted());
                        json.put("avgDuration", avg == null ? 0.0 : avg);
                        json.put("timestamp", System.currentTimeMillis());
                        return json.toString();
                    } catch (Exception e) {
                        return "{}";
                    }
                });

        metrics.to(OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde));

        // ---------------- PER-CITY ACTIVE RIDES ----------------
        // Convert activeRides KTable to KStream
        KStream<String, Long> activeRidesStream = activeRides.toStream();

        // Map each city and count to JSON and send to OUTPUT_TOPIC
        activeRidesStream
                .map((city, count) -> {
                    try {
                        ObjectNode json = mapper.createObjectNode();
                        json.put("type", "CITY_ACTIVE_RIDES");
                        json.put("city", city);
                        json.put("activeRides", count);
                        json.put("timestamp", System.currentTimeMillis());
                        return KeyValue.pair(city, json.toString());
                    } catch (Exception e) {
                        return KeyValue.pair(city, "{}");
                    }
                })
                .to(OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde));

        // ---------------- TOP CITIES ON ACTIVE RIDES ----------------
        KStream<String, Long> activeRidesStreamForTopCities = activeRides.toStream();

        KStream<String, CityCount> cityCountStream = activeRidesStreamForTopCities
                .map((city, count) -> KeyValue.pair("TOP_CITIES_KEY", new CityCount(city, count)));

        KTable<String, Map<String, Long>> cityCountsTable2 = cityCountStream
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(CityCount.class)))
                .aggregate(
                        HashMap::new,
                        (aggKey, cityCount, state) -> {
                            if (cityCount.getCount() == 0) {
                                state.remove(cityCount.getCity());
                            } else {
                                state.put(cityCount.getCity(), cityCount.getCount());
                            }
                            return state;
                        },
                        Materialized.<String, Map<String, Long>, KeyValueStore<Bytes, byte[]>>as(
                                        "top-cities-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(
                                        new TypeReference<Map<String, Long>>() {
                                        })));

        // Convert map to list, sort and limit top 5, then write JSON string to output
        // topic
        cityCountsTable2.toStream()
                .mapValues(map -> {
                    List<CityCount> top5 = map.entrySet().stream()
                            .map(e -> new CityCount(e.getKey(), e.getValue()))
                            .sorted((c1, c2) -> Long.compare(c2.getCount(), c1.getCount()))
                            .limit(5)
                            .collect(Collectors.toList());
                    ObjectNode json = mapper.createObjectNode();
                    json.put("type", "TOP_ACTIVE_CITIES");
                    json.putPOJO("cities", top5);
                    json.put("timestamp", System.currentTimeMillis());
                    return json.toString();
                })
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // -------------- ANOMALIES ----------------
        events.filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED &&
                        v.getDurationMinutes() != null &&
                        v.getDurationMinutes() > LONG_RIDE_THRESHOLD_MINUTES)
                .mapValues(event -> {
                    try {
                        ObjectNode anomaly = mapper.createObjectNode();
                        anomaly.put("type", "ANOMALY");
                        anomaly.put("city", event.getCity());
                        anomaly.put("rideId", event.getRideId());
                        anomaly.put("duration", event.getDurationMinutes());
                        anomaly.put("message", "Long ride detected");
                        anomaly.put("timestamp", System.currentTimeMillis());
                        return anomaly.toString();
                    } catch (Exception e) {
                        return "";
                    }
                })
                .to(OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde));

        log.info("Ride Analytics Processor initialized.");
    }

}
