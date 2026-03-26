package com.iwos.darkstore.controller;

import com.iwos.darkstore.entity.DarkStore;
import com.iwos.darkstore.service.DarkStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/darkstores") @RequiredArgsConstructor
public class DarkStoreController {
    private final DarkStoreService darkStoreService;

    @PostMapping
    public ResponseEntity<DarkStore> create(@RequestBody DarkStore store) {
        return ResponseEntity.status(HttpStatus.CREATED).body(darkStoreService.createStore(store));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DarkStore> get(@PathVariable String id) {
        return ResponseEntity.ok(darkStoreService.getStore(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DarkStore>> findNearby(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(darkStoreService.findNearbyStores(lat, lng));
    }

    @GetMapping
    public ResponseEntity<List<DarkStore>> getAll() {
        return ResponseEntity.ok(darkStoreService.getAll());
    }
}
