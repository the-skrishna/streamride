package com.streamride.processorservice.aggregator;

import com.streamride.model.EventType;
import com.streamride.model.RideEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 * Aggregator for calculating total and average ride duration per city.
 * 
 * Provides two KTables:
 * 1. Completed rides count per city
 * 2. Average ride duration per city (total duration / count)
 */
@Component
public class TotalDurationAggregator {

    private static final String RIDES_COUNT_STORE = "rides-count-store";
    private static final String TOTAL_DURATION_STORE = "total-duration-store";
    private static final String AVG_DURATION_STORE = "avg-duration-store";

    /**
     * Builds a KTable counting completed rides per city.
     * 
     * @param events source stream of ride events
     * @return KTable mapping city to completed ride count
     */
    public KTable<String, Long> buildCompletedRidesTable(KStream<String, RideEvent> events) {
        return events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED)
                .groupBy((k, v) -> v.getCity(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(RideEvent.class)))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(RIDES_COUNT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));
    }

    /**
     * Builds a KTable calculating average ride duration per city.
     * 
     * Computes average by dividing total duration by completed ride count.
     * 
     * @param events         source stream of ride events
     * @param completedRides KTable of completed ride counts (from
     *                       buildCompletedRidesTable)
     * @return KTable mapping city to average duration in minutes
     */
    public KTable<String, Double> buildAverageDurationTable(
            KStream<String, RideEvent> events,
            KTable<String, Long> completedRides) {

        // Calculate total duration per city
        KTable<String, Long> totalDuration = events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED &&
                        v.getDurationMinutes() != null)
                .groupBy((k, v) -> v.getCity(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(RideEvent.class)))
                .aggregate(
                        () -> 0L,
                        (city, event, total) -> total + event.getDurationMinutes(),
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(TOTAL_DURATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));

        // Calculate average: total duration / completed rides count
        return totalDuration.join(
                completedRides,
                (total, count) -> count == 0 ? 0.0 : (double) total / count,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(AVG_DURATION_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Double()));
    }
}
