package com.streamride.processorservice.aggregator;

import com.streamride.model.EventType;
import com.streamride.model.RideEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 * Aggregator for tracking active rides per city.
 * 
 * Active rides are incremented when a ride starts (RIDE_STARTED event)
 * and decremented when a ride completes (RIDE_COMPLETED event).
 * 
 * This aggregator is reused across multiple pipelines:
 * - MetricsPipeline
 * - CityActiveRidesPipeline
 * - TopCitiesPipeline
 */
@Component
public class ActiveRidesAggregator {

    private static final String STATE_STORE_NAME = "active-rides-store";

    /**
     * Builds a KTable tracking active rides per city.
     * 
     * @param events source stream of ride events
     * @return KTable mapping city name to active ride count
     */
    public KTable<String, Long> build(KStream<String, RideEvent> events) {
        return events
                .filter((k, v) -> v.getEventType() == EventType.RIDE_STARTED ||
                        v.getEventType() == EventType.RIDE_COMPLETED)
                .groupBy((k, v) -> v.getCity(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(RideEvent.class)))
                .aggregate(
                        () -> 0L,
                        (city, event, currentCount) -> {
                            if (event.getEventType() == EventType.RIDE_STARTED) {
                                return currentCount + 1; // Increment when ride starts
                            } else if (event.getEventType() == EventType.RIDE_COMPLETED) {
                                return Math.max(0, currentCount - 1); // Decrement when ride completes
                            }
                            return currentCount;
                        },
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(STATE_STORE_NAME)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));
    }
}
