package com.iwos.catalog.controller;

import com.iwos.catalog.dto.response.BrandResponse;
import com.iwos.catalog.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponse> create(
            @RequestParam String name,
            @RequestParam(required = false) String logoUrl,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(name, logoUrl, description));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(brandService.getBrand(id));
    }
}
