package com.iwos.seller.controller;

import com.iwos.seller.entity.Seller;
import com.iwos.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/sellers") @RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<Seller> register(@RequestBody Seller seller) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.registerSeller(seller));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Seller> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(sellerService.getSellerByUserId(userId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Seller> approve(@PathVariable String id) {
        return ResponseEntity.ok(sellerService.approveSeller(id));
    }
}
