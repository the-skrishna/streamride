package com.streamride.producerservice.controller;

import com.streamride.producerservice.service.RideEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final RideEventProducer simulator;

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startProducer() {
        simulator.start();
        return ResponseEntity.ok(Map.of("status", "Producer started"));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopProducer() {
        simulator.stop();
        return ResponseEntity.ok(Map.of("status", "Producer stopped"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "running", simulator.isRunning(),
            "status", simulator.isRunning() ? "active" : "inactive"
        ));
    }
}