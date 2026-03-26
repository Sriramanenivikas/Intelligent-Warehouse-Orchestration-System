package com.iwos.serviceability.controller;

import com.iwos.serviceability.entity.DeliveryPromise;
import com.iwos.serviceability.service.ServiceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/serviceability") @RequiredArgsConstructor
public class ServiceabilityController {
    private final ServiceabilityService service;

    @GetMapping("/check")
    public ResponseEntity<DeliveryPromise> checkByLocation(
            @RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(service.checkServiceability(lat, lng));
    }

    @GetMapping("/check/pincode/{pincode}")
    public ResponseEntity<DeliveryPromise> checkByPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(service.checkServiceabilityByPincode(pincode));
    }
}
