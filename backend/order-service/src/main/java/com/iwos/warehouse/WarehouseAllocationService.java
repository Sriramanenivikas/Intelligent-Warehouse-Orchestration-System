package com.iwos.warehouse;

import com.iwos.cqrs.command.CreateOrderCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Warehouse Allocation Service
 *
 * Implements intelligent warehouse selection algorithm based on:
 * 1. Geographic proximity (Haversine distance)
 * 2. Inventory availability
 * 3. Warehouse capacity
 * 4. Delivery type (EXPRESS requires closer warehouses)
 *
 * Future enhancements:
 * - Real-time inventory checking via Inventory Service
 * - ML-based optimization considering historical data
 * - Multi-warehouse order fulfillment
 * - Load balancing across warehouses
 */
@Service
@Slf4j
public class WarehouseAllocationService {

    private final Random random = new Random();

    /**
     * Find optimal warehouse for order fulfillment
     *
     * @param items List of items in the order
     * @param customerLat Customer delivery latitude
     * @param customerLon Customer delivery longitude
     * @param deliveryType EXPRESS or STANDARD
     * @return Optimal warehouse or null if none available
     */
    public Warehouse findOptimalWarehouse(
            List<CreateOrderCommand.OrderItemDTO> items,
            Double customerLat,
            Double customerLon,
            String deliveryType) {

        log.info("🔍 Finding optimal warehouse for location: ({}, {}), delivery type: {}",
                customerLat, customerLon, deliveryType);

        // Get all available warehouses (stub data for now)
        List<Warehouse> availableWarehouses = getAvailableWarehouses();

        // Calculate distances and scores for each warehouse
        for (Warehouse warehouse : availableWarehouses) {
            double distance = calculateDistance(
                    customerLat, customerLon,
                    warehouse.getLatitude(), warehouse.getLongitude()
            );
            warehouse.setDistanceFromCustomer(distance);

            // Calculate estimated delivery time (simplified)
            int deliveryMinutes = calculateDeliveryTime(distance, deliveryType);
            warehouse.setEstimatedDeliveryMinutes(deliveryMinutes);

            // Check inventory availability (stub - always true for now)
            warehouse.setHasAllItems(true);
            warehouse.setInventoryScore(100);

            log.debug("Warehouse {} - Distance: {} km, ETA: {} mins",
                    warehouse.getName(), String.format("%.2f", distance), deliveryMinutes);
        }

        // Filter based on delivery type constraints
        List<Warehouse> eligibleWarehouses = filterByDeliveryType(availableWarehouses, deliveryType);

        if (eligibleWarehouses.isEmpty()) {
            log.warn("⚠️ No eligible warehouses found for delivery type: {}", deliveryType);
            return null;
        }

        // Select warehouse with best score (lowest distance)
        Warehouse optimalWarehouse = eligibleWarehouses.stream()
                .min(Comparator.comparingDouble(w -> w.calculateAllocationScore()))
                .orElse(null);

        if (optimalWarehouse != null) {
            log.info("✅ Selected warehouse: {} at {} km, ETA: {} mins",
                    optimalWarehouse.getName(),
                    String.format("%.2f", optimalWarehouse.getDistanceFromCustomer()),
                    optimalWarehouse.getEstimatedDeliveryMinutes());
        }

        return optimalWarehouse;
    }

    /**
     * Get list of available warehouses
     * TODO: Replace with actual Warehouse Service API call
     */
    private List<Warehouse> getAvailableWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();

        // Stub warehouses in different Indian cities
        warehouses.add(Warehouse.builder()
                .id("WH-BLR-001")
                .name("Bangalore Central Warehouse")
                .address("Electronic City Phase 1")
                .city("Bangalore")
                .state("Karnataka")
                .pincode("560100")
                .latitude(12.8456)
                .longitude(77.6603)
                .build());

        warehouses.add(Warehouse.builder()
                .id("WH-BLR-002")
                .name("Bangalore North Warehouse")
                .address("Yelahanka")
                .city("Bangalore")
                .state("Karnataka")
                .pincode("560064")
                .latitude(13.1007)
                .longitude(77.5963)
                .build());

        warehouses.add(Warehouse.builder()
                .id("WH-MUM-001")
                .name("Mumbai Central Warehouse")
                .address("Andheri East")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode("400069")
                .latitude(19.1136)
                .longitude(72.8697)
                .build());

        warehouses.add(Warehouse.builder()
                .id("WH-DEL-001")
                .name("Delhi NCR Warehouse")
                .address("Gurugram Sector 18")
                .city("Gurugram")
                .state("Haryana")
                .pincode("122015")
                .latitude(28.4920)
                .longitude(77.0836)
                .build());

        warehouses.add(Warehouse.builder()
                .id("WH-HYD-001")
                .name("Hyderabad Warehouse")
                .address("Hitech City")
                .city("Hyderabad")
                .state("Telangana")
                .pincode("500081")
                .latitude(17.4435)
                .longitude(78.3772)
                .build());

        return warehouses;
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     *
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Calculate estimated delivery time based on distance and delivery type
     *
     * @param distance Distance in kilometers
     * @param deliveryType EXPRESS or STANDARD
     * @return Estimated delivery time in minutes
     */
    private int calculateDeliveryTime(double distance, String deliveryType) {
        // Base speeds (km/h)
        double expressSpeed = 40.0;  // Faster, direct route
        double standardSpeed = 25.0; // Slower, may have multiple stops

        double speed = "EXPRESS".equals(deliveryType) ? expressSpeed : standardSpeed;

        // Time = Distance / Speed (in hours), convert to minutes
        int travelTime = (int) Math.ceil((distance / speed) * 60);

        // Add handling time (warehouse processing + loading)
        int handlingTime = "EXPRESS".equals(deliveryType) ? 15 : 30;

        return travelTime + handlingTime;
    }

    /**
     * Filter warehouses based on delivery type constraints
     */
    private List<Warehouse> filterByDeliveryType(List<Warehouse> warehouses, String deliveryType) {
        if ("EXPRESS".equals(deliveryType)) {
            // For express delivery, only consider warehouses within 50 km
            return warehouses.stream()
                    .filter(w -> w.getDistanceFromCustomer() <= 50.0)
                    .toList();
        } else {
            // For standard delivery, consider warehouses within 200 km
            return warehouses.stream()
                    .filter(w -> w.getDistanceFromCustomer() <= 200.0)
                    .toList();
        }
    }

    /**
     * Check inventory availability at warehouse
     * TODO: Integrate with Inventory Service
     */
    private boolean checkInventoryAvailability(String warehouseId, List<CreateOrderCommand.OrderItemDTO> items) {
        // Stub implementation - always return true
        // In production, this would call the Inventory Service API
        return true;
    }
}
