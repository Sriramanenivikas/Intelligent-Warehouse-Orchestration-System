package com.iwos.controller;

import com.iwos.dto.*;
import com.iwos.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InventoryController
 * Handles all inventory-related HTTP endpoints
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * POST /api/v1/inventory - Add inventory
     *
     * @param request AddInventoryRequest containing SKU, warehouse, and quantity
     * @return InventoryResponse with updated inventory details
     */
    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@Valid @RequestBody AddInventoryRequest request) {
        log.info("POST /api/v1/inventory - Adding inventory: SKU {}, Warehouse {}, Quantity {}",
                request.getSkuId(), request.getWarehouseId(), request.getQuantity());

        try {
            InventoryResponse response = inventoryService.addInventory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error adding inventory", e);
            throw new RuntimeException("Failed to add inventory: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/inventory/{warehouseId}/{skuId} - Get inventory level
     *
     * @param warehouseId Warehouse ID
     * @param skuId SKU ID
     * @return InventoryLevelResponse with current inventory levels
     */
    @GetMapping("/{warehouseId}/{skuId}")
    public ResponseEntity<InventoryLevelResponse> getInventoryLevel(
            @PathVariable Long warehouseId,
            @PathVariable Long skuId) {
        log.info("GET /api/v1/inventory/{}/{} - Getting inventory level", warehouseId, skuId);

        try {
            InventoryLevelResponse response = inventoryService.getInventoryLevel(warehouseId, skuId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting inventory level", e);
            throw new RuntimeException("Failed to get inventory level: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/inventory/reserve - Reserve inventory for order
     *
     * @param request ReserveInventoryRequest with order and items to reserve
     * @return ReservationResponse with reservation status
     */
    @PutMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveInventory(
            @Valid @RequestBody ReserveInventoryRequest request) {
        log.info("PUT /api/v1/inventory/reserve - Reserving inventory for order {}", request.getOrderId());

        try {
            ReservationResponse response = inventoryService.reserveInventory(request);

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
        } catch (Exception e) {
            log.error("Error reserving inventory", e);
            throw new RuntimeException("Failed to reserve inventory: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/inventory/release - Release reserved inventory
     *
     * @param request ReleaseInventoryRequest with order and items to release
     * @return ReservationResponse with release status
     */
    @PutMapping("/release")
    public ResponseEntity<ReservationResponse> releaseInventory(
            @Valid @RequestBody ReleaseInventoryRequest request) {
        log.info("PUT /api/v1/inventory/release - Releasing inventory for order {}", request.getOrderId());

        try {
            ReservationResponse response = inventoryService.releaseInventory(request);

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
        } catch (Exception e) {
            log.error("Error releasing inventory", e);
            throw new RuntimeException("Failed to release inventory: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/inventory/warehouse/{warehouseId} - Get all inventory for warehouse
     *
     * @param warehouseId Warehouse ID
     * @return List of InventoryResponse for all items in warehouse
     */
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getWarehouseInventory(
            @PathVariable Long warehouseId) {
        log.info("GET /api/v1/inventory/warehouse/{} - Getting all inventory for warehouse", warehouseId);

        try {
            List<InventoryResponse> response = inventoryService.getWarehouseInventory(warehouseId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting warehouse inventory", e);
            throw new RuntimeException("Failed to get warehouse inventory: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/inventory/low-stock - Get low stock items
     *
     * @param warehouseId Optional warehouse ID filter
     * @return List of InventoryLevelResponse for low stock items
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryLevelResponse>> getLowStockItems(
            @RequestParam(required = false) Long warehouseId) {
        log.info("GET /api/v1/inventory/low-stock - Getting low stock items for warehouse {}",
                warehouseId != null ? warehouseId : "all");

        try {
            List<InventoryLevelResponse> response = inventoryService.getLowStockItems(warehouseId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting low stock items", e);
            throw new RuntimeException("Failed to get low stock items: " + e.getMessage());
        }
    }
}
