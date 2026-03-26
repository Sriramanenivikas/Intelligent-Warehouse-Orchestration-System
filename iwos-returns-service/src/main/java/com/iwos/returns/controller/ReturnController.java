package com.iwos.returns.controller;

import com.iwos.returns.entity.ReturnRequest;
import com.iwos.returns.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/returns") @RequiredArgsConstructor
public class ReturnController {
    private final ReturnService returnService;

    @PostMapping
    public ResponseEntity<ReturnRequest> create(@RequestBody ReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createReturnRequest(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReturnRequest> approve(@PathVariable String id) {
        return ResponseEntity.ok(returnService.approveReturn(id));
    }

    @PutMapping("/{id}/schedule-pickup")
    public ResponseEntity<ReturnRequest> schedulePickup(
            @PathVariable String id, @RequestParam String address) {
        return ResponseEntity.ok(returnService.schedulePickup(id, address));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReturnRequest>> getByUser(@PathVariable String userId, Pageable pageable) {
        return ResponseEntity.ok(returnService.getReturnsByUser(userId, pageable));
    }
}
