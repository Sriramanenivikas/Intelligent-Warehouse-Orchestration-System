package com.iwos.seller.controller;

import com.iwos.seller.entity.Settlement;
import com.iwos.seller.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/sellers/{sellerId}/settlements") @RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<Page<Settlement>> getSettlements(
            @PathVariable String sellerId, Pageable pageable) {
        return ResponseEntity.ok(settlementService.getSettlements(sellerId, pageable));
    }
}
