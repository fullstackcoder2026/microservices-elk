package com.example.order.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PingController {

    private static final Logger log = LoggerFactory.getLogger(PingController.class);

    @Value("${spring.application.name}")
    private String serviceName;

    private final Tracer tracer;

    public PingController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        String requestId = MDC.get("requestId");
        
        log.info("Received ping request");

        // Simulate business logic
        try {
            Thread.sleep(50); // Simulate processing
            log.info("Business step simulated - processing order validation");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during processing", e);
        }

        // Get trace information
        Span currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null ? currentSpan.context().traceId() : "N/A";
        String spanId = currentSpan != null ? currentSpan.context().spanId() : "N/A";

        log.info("Sending response");

        Map<String, String> response = new HashMap<>();
        response.put("service", serviceName);
        response.put("status", "ok");
        response.put("requestId", requestId);
        response.put("traceId", traceId);
        response.put("spanId", spanId);

        return ResponseEntity.ok(response);
    }
}
