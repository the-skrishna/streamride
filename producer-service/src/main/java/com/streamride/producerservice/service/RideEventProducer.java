package com.streamride.producerservice.service;

import com.streamride.model.RideEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventProducer {

    private final KafkaTemplate<String, RideEvent> kafkaTemplate;
    private final RideEventGenerator generator;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private static final String TOPIC = "rides.events";

    @Scheduled(fixedRate = 3000)
    public void publishEvent() {
        if (!running.get()) {
            return;
        }
        RideEvent event = generator.generateRandomEvent();
        kafkaTemplate.send(TOPIC, event.getCity(), event);
        log.info("🚕 Produced event: {}", event);
    }

    public void start() {
        running.set(true);
        log.info("Producer started");
    }

    public void stop() {
        running.set(false);
        log.info("Producer stopped");
    }

    public boolean isRunning() {
        return running.get();
    }
}
