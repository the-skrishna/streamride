package com.streamride.processorservice.processor;

import com.streamride.model.RideEvent;
import com.streamride.processorservice.aggregator.ActiveRidesAggregator;
import com.streamride.processorservice.aggregator.TotalDurationAggregator;
import com.streamride.processorservice.pipeline.AnomalyPipeline;
import com.streamride.processorservice.pipeline.CityActiveRidesPipeline;
import com.streamride.processorservice.pipeline.MetricsPipeline;
import com.streamride.processorservice.pipeline.TopCitiesPipeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 * Orchestrator for ride analytics processing.
 * 
 * This class coordinates multiple analytics pipelines that consume ride events
 * and produce various analytics to the output topic. Each pipeline is
 * responsible
 * for a specific analytics stream:
 * 
 * - {@link MetricsPipeline}: Aggregated metrics (active rides, completed rides,
 * avg duration)
 * - {@link CityActiveRidesPipeline}: Per-city active ride counts
 * - {@link TopCitiesPipeline}: Top 5 cities by active rides
 * - {@link AnomalyPipeline}: Long ride detection
 * 
 * The orchestrator follows the Single Responsibility Principle by delegating
 * all processing logic to dedicated pipeline classes, making the codebase
 * highly maintainable and testable.
 */
@Component
@Slf4j
public class RideAnalyticsProcessor {

    private static final String SOURCE_TOPIC = "rides.events";

    private final ActiveRidesAggregator activeRidesAggregator;
    private final TotalDurationAggregator durationAggregator;
    private final MetricsPipeline metricsPipeline;
    private final CityActiveRidesPipeline cityActiveRidesPipeline;
    private final TopCitiesPipeline topCitiesPipeline;
    private final AnomalyPipeline anomalyPipeline;

    /**
     * Constructor with dependency injection.
     * 
     * @param activeRidesAggregator   aggregator for active rides
     * @param durationAggregator      aggregator for duration calculations
     * @param metricsPipeline         pipeline for aggregated metrics
     * @param cityActiveRidesPipeline pipeline for per-city active rides
     * @param topCitiesPipeline       pipeline for top cities ranking
     * @param anomalyPipeline         pipeline for anomaly detection
     */
    @Autowired
    public RideAnalyticsProcessor(
            ActiveRidesAggregator activeRidesAggregator,
            TotalDurationAggregator durationAggregator,
            MetricsPipeline metricsPipeline,
            CityActiveRidesPipeline cityActiveRidesPipeline,
            TopCitiesPipeline topCitiesPipeline,
            AnomalyPipeline anomalyPipeline) {
        this.activeRidesAggregator = activeRidesAggregator;
        this.durationAggregator = durationAggregator;
        this.metricsPipeline = metricsPipeline;
        this.cityActiveRidesPipeline = cityActiveRidesPipeline;
        this.topCitiesPipeline = topCitiesPipeline;
        this.anomalyPipeline = anomalyPipeline;
    }

    /**
     * Main processing method - orchestrates all analytics pipelines.
     * 
     * Creates the source stream and builds shared aggregation tables once,
     * then delegates to each pipeline for processing. This prevents duplicate
     * processor registration in the Kafka Streams topology.
     * 
     * @param builder Kafka StreamsBuilder for topology construction
     */
    @Autowired
    public void process(StreamsBuilder builder) {
        // Create source stream
        KStream<String, RideEvent> events = createSourceStream(builder);

        // Build shared aggregation tables once
        KTable<String, Long> activeRides = activeRidesAggregator.build(events);
        KTable<String, Long> completedRides = durationAggregator.buildCompletedRidesTable(events);
        KTable<String, Double> avgDuration = durationAggregator.buildAverageDurationTable(events, completedRides);

        // Execute all analytics pipelines with shared tables
        metricsPipeline.build(activeRides, completedRides, avgDuration);
        cityActiveRidesPipeline.build(activeRides);
        topCitiesPipeline.build(activeRides);
        anomalyPipeline.build(events);

        log.info("Ride Analytics Processor initialized with 4 pipelines.");
    }

    /**
     * Creates the source stream from the rides.events topic.
     * 
     * @param builder Kafka StreamsBuilder
     * @return KStream of ride events
     */
    private KStream<String, RideEvent> createSourceStream(StreamsBuilder builder) {
        JsonSerde<RideEvent> rideEventSerde = new JsonSerde<>(RideEvent.class);
        return builder.stream(SOURCE_TOPIC, Consumed.with(Serdes.String(), rideEventSerde));
    }
}
