package com.iwos.loadtest.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * LOAD TEST CONTROLLER
 *
 * REST API to control load testing
 * - Start/stop load generation
 * - Adjust rate dynamically
 * - Get current metrics
 */
@RestController
@RequestMapping("/api/loadtest")
@Slf4j
public class LoadTestController {

    @Value("${loadtest.orders-per-second:10000}")
    private int ordersPerSecond;

    @Value("${loadtest.enabled:false}")
    private boolean loadTestEnabled;

    private final MeterRegistry meterRegistry;

    public LoadTestController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Start load testing
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startLoadTest(@RequestParam(required = false) Integer rate) {
        if (rate != null && rate > 0) {
            this.ordersPerSecond = rate;
        }

        this.loadTestEnabled = true;

        log.info("🚀 Load test STARTED - Target: {} orders/sec", ordersPerSecond);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "started");
        response.put("targetRate", ordersPerSecond);
        response.put("message", "Load test started successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Stop load testing
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopLoadTest() {
        this.loadTestEnabled = false;

        log.info("🛑 Load test STOPPED");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "stopped");
        response.put("message", "Load test stopped successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Adjust load test rate
     */
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> adjustRate(@RequestParam int rate) {
        if (rate <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Rate must be positive"
            ));
        }

        this.ordersPerSecond = rate;

        log.info("⚡ Load test rate adjusted to {} orders/sec", rate);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "adjusted");
        response.put("newRate", rate);
        response.put("message", "Rate adjusted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get current load test status and metrics
     */
    @GetMapping("/status")
    public ResponseEntity<LoadTestStatus> getStatus() {
        Counter generated = meterRegistry.find("orders.generated.total").counter();
        Counter success = meterRegistry.find("orders.success.total").counter();
        Counter failure = meterRegistry.find("orders.failure.total").counter();

        double generatedCount = generated != null ? generated.count() : 0;
        double successCount = success != null ? success.count() : 0;
        double failureCount = failure != null ? failure.count() : 0;

        double successRate = generatedCount > 0
            ? (successCount / generatedCount) * 100
            : 0;

        LoadTestStatus status = new LoadTestStatus();
        status.setEnabled(loadTestEnabled);
        status.setTargetRate(ordersPerSecond);
        status.setOrdersGenerated((long) generatedCount);
        status.setOrdersSuccess((long) successCount);
        status.setOrdersFailure((long) failureCount);
        status.setSuccessRate(successRate);

        return ResponseEntity.ok(status);
    }

    @Data
    public static class LoadTestStatus {
        private boolean enabled;
        private int targetRate;
        private long ordersGenerated;
        private long ordersSuccess;
        private long ordersFailure;
        private double successRate;
    }
}
