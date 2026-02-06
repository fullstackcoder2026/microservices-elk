package com.example.payment.scheduler;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RandomLogGenerator {

    private static final Logger log = LoggerFactory.getLogger(RandomLogGenerator.class);
    private final Tracer tracer;
    private final Random random = new Random();

    private static final String[] PAYMENT_EVENTS = {
        "Payment initiated",
        "Payment authorized",
        "Payment captured",
        "Payment settled",
        "Refund processed",
        "Payment failed"
    };

    private static final String[] LOG_LEVELS = {"INFO", "INFO", "INFO", "WARN", "ERROR"};

    public RandomLogGenerator(Tracer tracer) {
        this.tracer = tracer;
    }

    @Scheduled(fixedDelay = 4, timeUnit = TimeUnit.SECONDS)
    public void generateRandomLog() {
        // Create a new span for this scheduled event
        Span span = tracer.nextSpan().name("scheduled-log-generation").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            // Generate a new requestId for this event
            String requestId = UUID.randomUUID().toString();
            String traceId = span.context().traceId();
            String spanId = span.context().spanId();

            // Add to MDC
            MDC.put("requestId", requestId);
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);

            try {
                // Random event
                String event = PAYMENT_EVENTS[random.nextInt(PAYMENT_EVENTS.length)];
                String paymentId = "PAY-" + random.nextInt(10000);
                double amount = 10.0 + (random.nextDouble() * 990.0);
                String logLevel = LOG_LEVELS[random.nextInt(LOG_LEVELS.length)];

                String message = String.format("%s for paymentId=%s, amount=%.2f", 
                    event, paymentId, amount);

                switch (logLevel) {
                    case "INFO":
                        log.info(message);
                        break;
                    case "WARN":
                        log.warn(message + " - fraud check pending");
                        break;
                    case "ERROR":
                        log.error(message + " - insufficient funds");
                        break;
                }
            } finally {
                // Clean up MDC
                MDC.remove("requestId");
                MDC.remove("traceId");
                MDC.remove("spanId");
            }
        } finally {
            span.end();
        }
    }
}
