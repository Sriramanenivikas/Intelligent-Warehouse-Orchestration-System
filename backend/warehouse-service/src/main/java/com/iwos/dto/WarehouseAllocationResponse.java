package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for warehouse allocation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseAllocationResponse {

    private String orderId;
    private WarehouseResponse allocatedWarehouse;
    private Double distanceKm;
    private String deliveryType;
    private Integer estimatedDeliveryMinutes;
    private AllocationStatus status;
    private String message;
    private LocalDateTime timestamp;

    // Additional allocation details
    private AllocationMetrics metrics;
    private List<WarehouseResponse> alternativeWarehouses;

    public enum AllocationStatus {
        SUCCESS,
        NO_WAREHOUSE_FOUND,
        INSUFFICIENT_INVENTORY,
        OUT_OF_RANGE,
        ERROR
    }

    /**
     * Metrics for allocation decision
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationMetrics {
        private Integer candidatesEvaluated;
        private Integer candidatesWithInventory;
        private Long executionTimeMs;
        private Double maxDistanceKm;
        private String algorithm;
    }

    /**
     * Create success response
     */
    public static WarehouseAllocationResponse success(
            String orderId,
            WarehouseResponse warehouse,
            Double distanceKm,
            String deliveryType,
            AllocationMetrics metrics) {

        // Estimate delivery time based on distance and delivery type
        int estimatedMinutes = calculateEstimatedDeliveryTime(distanceKm, deliveryType);

        return WarehouseAllocationResponse.builder()
            .orderId(orderId)
            .allocatedWarehouse(warehouse)
            .distanceKm(distanceKm)
            .deliveryType(deliveryType)
            .estimatedDeliveryMinutes(estimatedMinutes)
            .status(AllocationStatus.SUCCESS)
            .message("Warehouse allocated successfully")
            .timestamp(LocalDateTime.now())
            .metrics(metrics)
            .build();
    }

    /**
     * Create failure response
     */
    public static WarehouseAllocationResponse failure(
            String orderId,
            AllocationStatus status,
            String message,
            AllocationMetrics metrics) {

        return WarehouseAllocationResponse.builder()
            .orderId(orderId)
            .status(status)
            .message(message)
            .timestamp(LocalDateTime.now())
            .metrics(metrics)
            .build();
    }

    /**
     * Calculate estimated delivery time based on distance and delivery type
     */
    private static int calculateEstimatedDeliveryTime(Double distanceKm, String deliveryType) {
        if (distanceKm == null) return 0;

        // Average speed assumptions:
        // EXPRESS: 40 km/h (urban traffic)
        // STANDARD: 30 km/h (regular delivery)
        double speedKmPerHour = "EXPRESS".equals(deliveryType) ? 40.0 : 30.0;

        // Add base preparation time
        int prepTimeMinutes = "EXPRESS".equals(deliveryType) ? 15 : 30;

        // Calculate travel time
        double travelTimeMinutes = (distanceKm / speedKmPerHour) * 60;

        return prepTimeMinutes + (int) Math.ceil(travelTimeMinutes);
    }
}
