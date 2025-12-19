package com.streamride.processorservice.pipeline;

import com.streamride.model.EventType;
import com.streamride.model.RideEvent;
import com.streamride.processorservice.mapper.AnomalyJsonMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

/**
 * Pipeline for detecting and publishing ride anomalies.
 * 
 * Detects rides longer than the duration threshold and emits ANOMALY messages.
 */
@Component
public class AnomalyPipeline {

    private static final String OUTPUT_TOPIC = "rides.analytics";
    private static final int LONG_RIDE_THRESHOLD_MINUTES = 60;

    private final AnomalyJsonMapper jsonMapper;

    public AnomalyPipeline(AnomalyJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Builds and executes the anomaly detection pipeline.
     * 
     * @param events source stream of ride events
     */
    public void build(KStream<String, RideEvent> events) {
        events.filter((k, v) -> v.getEventType() == EventType.RIDE_COMPLETED &&
                v.getDurationMinutes() != null &&
                v.getDurationMinutes() > LONG_RIDE_THRESHOLD_MINUTES)
                .mapValues(jsonMapper::createAnomalyJson)
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
}
