package com.iwos.allocation;

import com.iwos.domain.Warehouse;
import com.iwos.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * INTELLIGENT WAREHOUSE ALLOCATION SERVICE
 *
 * This is the CORE ALGORITHM that makes the system smart!
 *
 * Problem: Given an order, find the optimal warehouse to fulfill it from
 *
 * Constraints:
 * - Warehouse must have ALL required inventory
 * - Must be within delivery range (10km for EXPRESS, 50km for STANDARD)
 * - Consider current warehouse load
 * - Response time < 100ms
 *
 * Algorithm: Multi-Criteria Decision Making
 * - Distance from customer (40% weight)
 * - Inventory availability (30% weight)
 * - Current warehouse load (20% weight)
 * - Warehouse priority/rating (10% weight)
 *
 * Uses PostGIS for geospatial queries (fast!)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseAllocationService {

    private final WarehouseRepository warehouseRepository;

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Delivery range limits
    private static final double EXPRESS_RANGE_KM = 10.0;
    private static final double STANDARD_RANGE_KM = 50.0;

    /**
     * Find optimal warehouse for order
     *
     * @param items Required items
     * @param customerLat Customer latitude
     * @param customerLon Customer longitude
     * @param deliveryType EXPRESS or STANDARD
     * @return Best warehouse or null
     */
    public Warehouse findOptimalWarehouse(List<OrderItemDTO> items,
                                          Double customerLat,
                                          Double customerLon,
                                          String deliveryType) {

        long startTime = System.currentTimeMillis();

        log.info("🔍 Finding optimal warehouse for {} items near ({}, {})",
            items.size(), customerLat, customerLon);

        // 1. Determine search radius based on delivery type
        double maxDistanceKm = "EXPRESS".equals(deliveryType)
            ? EXPRESS_RANGE_KM
            : STANDARD_RANGE_KM;

        log.info("📏 Max distance: {} km ({})", maxDistanceKm, deliveryType);

        // 2. Find candidate warehouses (uses PostGIS spatial index)
        List<Warehouse> candidates = warehouseRepository
            .findWarehousesWithinRadius(customerLat, customerLon, maxDistanceKm);

        log.info("📍 Found {} candidate warehouses within {} km",
            candidates.size(), maxDistanceKm);

        if (candidates.isEmpty()) {
            log.warn("❌ No warehouses found within delivery range");
            return null;
        }

        // 3. Filter: Only warehouses with complete inventory
        List<String> requiredSkus = items.stream()
            .map(OrderItemDTO::getSku)
            .collect(Collectors.toList());

        candidates = candidates.stream()
            .filter(wh -> hasCompleteInventory(wh, items))
            .filter(Warehouse::isActive)
            .filter(wh -> wh.getCurrentLoad() < wh.getMaxCapacity())
            .collect(Collectors.toList());

        log.info("✅ {} warehouses have complete inventory", candidates.size());

        if (candidates.isEmpty()) {
            log.warn("❌ No warehouses with complete inventory available");
            return null;
        }

        // 4. Calculate distances and scores
        List<ScoredWarehouse> scoredWarehouses = candidates.stream()
            .map(wh -> {
                double distance = calculateDistance(
                    wh.getLatitude(),
                    wh.getLongitude(),
                    customerLat,
                    customerLon
                );
                double score = calculateScore(wh, distance, items);

                wh.setDistanceFromCustomer(distance);  // Store for response

                return new ScoredWarehouse(wh, distance, score);
            })
            .sorted(Comparator.comparing(ScoredWarehouse::getScore).reversed())
            .collect(Collectors.toList());

        // 5. Select best warehouse
        ScoredWarehouse best = scoredWarehouses.get(0);

        long elapsedMs = System.currentTimeMillis() - startTime;

        log.info("🏆 Best warehouse: {} (distance: {:.2f} km, score: {:.3f}, time: {} ms)",
            best.getWarehouse().getName(),
            best.getDistance(),
            best.getScore(),
            elapsedMs);

        // Log top 3 for debugging
        if (log.isDebugEnabled()) {
            log.debug("Top 3 warehouses:");
            scoredWarehouses.stream()
                .limit(3)
                .forEach(sw -> log.debug("  - {} (distance: {:.2f} km, score: {:.3f})",
                    sw.getWarehouse().getName(),
                    sw.getDistance(),
                    sw.getScore()));
        }

        return best.getWarehouse();
    }

    /**
     * Multi-Criteria Scoring Algorithm
     *
     * Factors:
     * 1. Distance Score (40%): Closer is better
     * 2. Inventory Score (30%): Exact match is better
     * 3. Load Score (20%): Lower load is better
     * 4. Priority Score (10%): Higher priority warehouses preferred
     *
     * @return Score between 0 and 1 (higher is better)
     */
    private double calculateScore(Warehouse warehouse, double distanceKm,
                                   List<OrderItemDTO> items) {

        // 1. Distance Score (inverse - closer is better)
        // Use exponential decay: e^(-distance/10)
        // At 0km: score = 1.0
        // At 5km: score = 0.606
        // At 10km: score = 0.368
        double distanceScore = Math.exp(-distanceKm / 10.0);

        // 2. Load Score (lower load is better)
        // 0% load → score = 1.0
        // 50% load → score = 0.5
        // 100% load → score = 0.0
        double loadPercentage = (double) warehouse.getCurrentLoad() / warehouse.getMaxCapacity();
        double loadScore = 1.0 - loadPercentage;

        // 3. Inventory Score (exact quantities available is better)
        double inventoryScore = calculateInventoryFitScore(warehouse, items);

        // 4. Priority Score (normalized 0-1)
        // Priority ranges from 1-10, normalize to 0-1
        double priorityScore = warehouse.getPriority() / 10.0;

        // Weighted sum
        double finalScore =
            (distanceScore * 0.4) +
            (inventoryScore * 0.3) +
            (loadScore * 0.2) +
            (priorityScore * 0.1);

        return finalScore;
    }

    /**
     * Haversine Formula for accurate distance calculation
     *
     * Calculates great-circle distance between two points on Earth
     * Accurate to within 0.5%
     *
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1,
                                      double lat2, double lon2) {

        // Convert to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if warehouse has complete inventory for order
     */
    private boolean hasCompleteInventory(Warehouse warehouse, List<OrderItemDTO> items) {
        // TODO: Query inventory service
        // For now, assume we have inventory data in warehouse aggregate
        for (OrderItemDTO item : items) {
            Integer available = warehouse.getInventory().get(item.getSku());
            if (available == null || available < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate how well inventory fits order
     *
     * Perfect fit (exact quantities) → 1.0
     * Excess inventory → slightly lower score
     * Just enough → high score
     */
    private double calculateInventoryFitScore(Warehouse warehouse, List<OrderItemDTO> items) {
        double totalFitScore = 0.0;

        for (OrderItemDTO item : items) {
            Integer available = warehouse.getInventory().get(item.getSku());
            if (available == null || available < item.getQuantity()) {
                return 0.0;  // Not enough inventory
            }

            // Calculate fit for this item
            double ratio = (double) item.getQuantity() / available;
            // Prefer ratio close to 1.0 (using entire stock) or small fraction
            // Penalize having exactly the required amount (risky)
            double fitScore = ratio > 0.9 ? 0.8 : (ratio < 0.5 ? 1.0 : 0.9);
            totalFitScore += fitScore;
        }

        return totalFitScore / items.size();
    }

    /**
     * Helper class to hold warehouse with its score
     */
    private static class ScoredWarehouse {
        private final Warehouse warehouse;
        private final double distance;
        private final double score;

        public ScoredWarehouse(Warehouse warehouse, double distance, double score) {
            this.warehouse = warehouse;
            this.distance = distance;
            this.score = score;
        }

        public Warehouse getWarehouse() { return warehouse; }
        public double getDistance() { return distance; }
        public double getScore() { return score; }
    }
}

/**
 * DTO for order items
 */
class OrderItemDTO {
    private String sku;
    private Integer quantity;

    public String getSku() { return sku; }
    public Integer getQuantity() { return quantity; }
}
