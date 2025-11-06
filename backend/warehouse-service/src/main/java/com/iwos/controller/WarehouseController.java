package com.iwos.controller;

import com.iwos.allocation.WarehouseAllocationService;
import com.iwos.dto.*;
import com.iwos.entity.Warehouse;
import com.iwos.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Warehouse Controller
 * REST API endpoints for warehouse management and allocation
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseAllocationService allocationService;

    /**
     * Create a new warehouse
     * POST /api/v1/warehouses
     */
    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {

        log.info("Creating warehouse with code: {}", request.getCode());

        try {
            Warehouse warehouse = warehouseService.create(request);
            WarehouseResponse response = WarehouseResponse.fromEntity(warehouse);

            log.info("Successfully created warehouse with ID: {}", warehouse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Failed to create warehouse: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get warehouse by ID
     * GET /api/v1/warehouses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getWarehouse(@PathVariable Long id) {
        log.info("Fetching warehouse with ID: {}", id);

        return warehouseService.getById(id)
            .map(WarehouseResponse::fromEntity)
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                log.warn("Warehouse not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * List all warehouses
     * GET /api/v1/warehouses
     */
    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses(
            @RequestParam(required = false) Boolean activeOnly) {

        log.info("Fetching all warehouses (activeOnly: {})", activeOnly);

        List<Warehouse> warehouses = Boolean.TRUE.equals(activeOnly)
            ? warehouseService.getAllActive()
            : warehouseService.getAll();

        List<WarehouseResponse> response = warehouses.stream()
            .map(WarehouseResponse::fromEntity)
            .collect(Collectors.toList());

        log.info("Found {} warehouses", response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Update warehouse
     * PUT /api/v1/warehouses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        log.info("Updating warehouse with ID: {}", id);

        try {
            Warehouse warehouse = warehouseService.update(id, request);
            WarehouseResponse response = WarehouseResponse.fromEntity(warehouse);

            log.info("Successfully updated warehouse with ID: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Failed to update warehouse: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete warehouse
     * DELETE /api/v1/warehouses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        log.info("Deleting warehouse with ID: {}", id);

        try {
            warehouseService.delete(id);
            log.info("Successfully deleted warehouse with ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.error("Failed to delete warehouse: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Find optimal warehouse for order fulfillment
     * POST /api/v1/warehouses/allocate
     *
     * This is the CORE ENDPOINT that uses geospatial allocation!
     */
    @PostMapping("/allocate")
    public ResponseEntity<WarehouseAllocationResponse> allocateWarehouse(
            @Valid @RequestBody WarehouseAllocationRequest request) {

        log.info("Allocating warehouse for order: {}", request.getOrderId());
        log.info("Customer location: ({}, {}), Delivery type: {}",
            request.getCustomerLatitude(),
            request.getCustomerLongitude(),
            request.getDeliveryType());

        long startTime = System.currentTimeMillis();

        try {
            // Use allocation service to find optimal warehouse
            Warehouse optimalWarehouse = allocationService.findOptimalWarehouse(
                request.getItems(),
                request.getCustomerLatitude(),
                request.getCustomerLongitude(),
                request.getDeliveryType()
            );

            long executionTime = System.currentTimeMillis() - startTime;

            if (optimalWarehouse == null) {
                log.warn("No suitable warehouse found for order: {}", request.getOrderId());

                WarehouseAllocationResponse.AllocationMetrics metrics =
                    WarehouseAllocationResponse.AllocationMetrics.builder()
                        .executionTimeMs(executionTime)
                        .algorithm("Multi-Criteria Decision Making (Haversine + Scoring)")
                        .build();

                return ResponseEntity.ok(WarehouseAllocationResponse.failure(
                    request.getOrderId(),
                    WarehouseAllocationResponse.AllocationStatus.NO_WAREHOUSE_FOUND,
                    "No warehouse found within delivery range with required inventory",
                    metrics
                ));
            }

            // Convert to response DTO
            WarehouseResponse warehouseResponse = WarehouseResponse.fromEntity(optimalWarehouse);

            // Build allocation metrics
            WarehouseAllocationResponse.AllocationMetrics metrics =
                WarehouseAllocationResponse.AllocationMetrics.builder()
                    .executionTimeMs(executionTime)
                    .algorithm("Multi-Criteria Decision Making (Haversine + Scoring)")
                    .maxDistanceKm("EXPRESS".equals(request.getDeliveryType()) ? 10.0 : 50.0)
                    .build();

            WarehouseAllocationResponse response = WarehouseAllocationResponse.success(
                request.getOrderId(),
                warehouseResponse,
                optimalWarehouse.getDistanceFromCustomer(),
                request.getDeliveryType(),
                metrics
            );

            log.info("Successfully allocated warehouse {} for order {} in {} ms",
                optimalWarehouse.getCode(),
                request.getOrderId(),
                executionTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error allocating warehouse for order {}: {}",
                request.getOrderId(), e.getMessage(), e);

            long executionTime = System.currentTimeMillis() - startTime;

            WarehouseAllocationResponse.AllocationMetrics metrics =
                WarehouseAllocationResponse.AllocationMetrics.builder()
                    .executionTimeMs(executionTime)
                    .algorithm("Multi-Criteria Decision Making (Haversine + Scoring)")
                    .build();

            return ResponseEntity.ok(WarehouseAllocationResponse.failure(
                request.getOrderId(),
                WarehouseAllocationResponse.AllocationStatus.ERROR,
                "Error during allocation: " + e.getMessage(),
                metrics
            ));
        }
    }

    /**
     * Get warehouse statistics
     * GET /api/v1/warehouses/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWarehouseStats() {
        log.info("Fetching warehouse statistics");

        Long totalActive = warehouseService.countActive();
        List<Warehouse> allWarehouses = warehouseService.getAll();
        List<Warehouse> availableWarehouses = warehouseService.getWarehousesWithAvailableCapacity();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWarehouses", allWarehouses.size());
        stats.put("activeWarehouses", totalActive);
        stats.put("warehousesWithCapacity", availableWarehouses.size());
        stats.put("timestamp", LocalDateTime.now());

        // Calculate average load percentage
        double avgLoad = allWarehouses.stream()
            .filter(Warehouse::getIsActive)
            .mapToDouble(Warehouse::getLoadPercentage)
            .average()
            .orElse(0.0);
        stats.put("averageLoadPercentage", String.format("%.2f", avgLoad));

        return ResponseEntity.ok(stats);
    }

    /**
     * Get warehouses by city
     * GET /api/v1/warehouses/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<WarehouseResponse>> getWarehousesByCity(
            @PathVariable String city) {

        log.info("Fetching warehouses in city: {}", city);

        List<WarehouseResponse> response = warehouseService.getByCity(city).stream()
            .map(WarehouseResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Update warehouse inventory
     * PATCH /api/v1/warehouses/{id}/inventory
     */
    @PatchMapping("/{id}/inventory")
    public ResponseEntity<WarehouseResponse> updateInventory(
            @PathVariable Long id,
            @RequestParam String sku,
            @RequestParam Integer quantity) {

        log.info("Updating inventory for warehouse {}: SKU={}, Quantity={}",
            id, sku, quantity);

        try {
            Warehouse warehouse = warehouseService.updateInventory(id, sku, quantity);
            return ResponseEntity.ok(WarehouseResponse.fromEntity(warehouse));

        } catch (IllegalArgumentException e) {
            log.error("Failed to update inventory: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/warehouses/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "warehouse-service");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
