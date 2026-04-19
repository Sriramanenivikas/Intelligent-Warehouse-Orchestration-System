package com.iwos.shipmentnetwork.api.http;

import com.iwos.shipmentnetwork.application.ShipmentNetworkCommandService;
import com.iwos.shipmentnetwork.application.ShipmentNetworkQueryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/network-shipments")
public class ShipmentNetworkController {

    private final ShipmentNetworkCommandService commandService;
    private final ShipmentNetworkQueryService queryService;

    public ShipmentNetworkController(
            ShipmentNetworkCommandService commandService,
            ShipmentNetworkQueryService queryService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<NetworkShipmentResponse> getByShipmentId(@PathVariable UUID shipmentId) {
        return ResponseEntity.ok(queryService.getByShipmentId(shipmentId));
    }

    @GetMapping("/by-order-intent/{orderIntentId}")
    public ResponseEntity<NetworkShipmentResponse> getByOrderIntentId(@PathVariable UUID orderIntentId) {
        return ResponseEntity.ok(queryService.getByOrderIntentId(orderIntentId));
    }

    @GetMapping("/by-awb/{awbNumber}")
    public ResponseEntity<NetworkShipmentResponse> getByAwb(@PathVariable String awbNumber) {
        return ResponseEntity.ok(queryService.getByAwbNumber(awbNumber));
    }

    @PostMapping("/{shipmentId}/scans")
    public ResponseEntity<NetworkShipmentResponse> recordScan(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody ScanRequest request
    ) {
        NetworkShipmentResponse response = commandService.recordScan(shipmentId, request);
        return ResponseEntity.created(URI.create("/api/v1/network-shipments/" + shipmentId)).body(response);
    }
}
