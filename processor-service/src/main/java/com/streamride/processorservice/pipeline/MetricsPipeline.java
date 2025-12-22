package com.streamride.processorservice.pipeline;

import com.streamride.processorservice.model.CityMetrics;
import com.streamride.processorservice.mapper.MetricsJsonMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

/**
 * Pipeline for publishing aggregated metrics.
 * 
 * Combines active rides, completed rides, and average duration
 * into METRICS messages published to the output topic.
 */
@Component
public class MetricsPipeline {

    private static final String OUTPUT_TOPIC = "rides.analytics";

    private final MetricsJsonMapper jsonMapper;

    public MetricsPipeline(MetricsJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Builds and executes the metrics pipeline.
     * 
     * @param activeRides    KTable of active rides per city
     * @param completedRides KTable of completed rides per city
     * @param avgDuration    KTable of average duration per city
     */
    public void build(
            KTable<String, Long> activeRides,
            KTable<String, Long> completedRides,
            KTable<String, Double> avgDuration) {

        // Combine activeRides and completedRides into CityMetrics
        KStream<String, CityMetrics> combined = activeRides
                .toStream()
                .leftJoin(completedRides,
                        (active, completed) -> new CityMetrics(
                                active == null ? 0 : active,
                                completed == null ? 0 : completed));

        // Join with avgDuration and create METRICS JSON messages
        combined.join(avgDuration,
                (cityMetrics, avg) -> jsonMapper.createMetricsJson(cityMetrics, avg))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
    }
}
