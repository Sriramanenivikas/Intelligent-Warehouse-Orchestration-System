package com.iwos.darkstore.controller;

import com.iwos.darkstore.entity.DarkStoreStock;
import com.iwos.darkstore.service.DarkStoreInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/v1/darkstores/{storeId}/inventory") @RequiredArgsConstructor
public class DarkStoreInventoryController {
    private final DarkStoreInventoryService inventoryService;

    @PostMapping("/stock")
    public ResponseEntity<DarkStoreStock> addStock(@PathVariable String storeId,
            @RequestParam String skuCode, @RequestParam String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.addStock(storeId, skuCode, productId, quantity));
    }

    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Boolean>> reserve(@PathVariable String storeId,
            @RequestParam String skuCode, @RequestParam int quantity) {
        return ResponseEntity.ok(Map.of("reserved", inventoryService.reserveStock(storeId, skuCode, quantity)));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable String storeId,
            @RequestParam String skuCode, @RequestParam int quantity) {
        return ResponseEntity.ok(Map.of("available", inventoryService.checkAvailability(storeId, skuCode, quantity)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<DarkStoreStock>> getLowStock(@PathVariable String storeId) {
        return ResponseEntity.ok(inventoryService.getLowStockItems(storeId));
    }
}
