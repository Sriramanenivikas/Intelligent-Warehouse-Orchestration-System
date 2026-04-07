package com.iwos.shipmenthandoff.api.http;

import com.iwos.shipmenthandoff.application.ShipmentCommandService;
import com.iwos.shipmenthandoff.application.ShipmentQueryService;
import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentQueryService queryService;
    private final ShipmentCommandService commandService;

    public ShipmentController(
            ShipmentQueryService queryService,
            ShipmentCommandService commandService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @PostMapping("/fulfillment-order/{fulfillmentOrderId}")
    public ResponseEntity<ShipmentResponse> createShipment(
            @PathVariable("fulfillmentOrderId") UUID fulfillmentOrderId,
            @Valid @RequestBody(required = false) @Nullable CreateShipmentRequest request
    ) {
        CarrierCode carrier = request != null && request.carrierCode() != null ? request.carrierCode() : CarrierCode.INTERNAL;
        Integer weight = request != null ? request.weightGrams() : null;
        Integer packages = request != null ? request.packageCount() : null;

        ShipmentResponse response = commandService.createShipment(fulfillmentOrderId, carrier, weight, packages);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shipmentId}")
    public ShipmentResponse getById(@PathVariable("shipmentId") UUID shipmentId) {
        return queryService.getById(shipmentId);
    }

    @GetMapping("/by-fulfillment-order/{fulfillmentOrderId}")
    public ShipmentResponse getByFulfillmentOrderId(@PathVariable("fulfillmentOrderId") UUID fulfillmentOrderId) {
        return queryService.getByFulfillmentOrderId(fulfillmentOrderId);
    }

    @GetMapping("/by-order-intent/{orderIntentId}")
    public ShipmentResponse getByOrderIntentId(@PathVariable("orderIntentId") UUID orderIntentId) {
        return queryService.getByOrderIntentId(orderIntentId);
    }

    @GetMapping("/by-awb/{awbNumber}")
    public ShipmentResponse getByAwbNumber(@PathVariable("awbNumber") String awbNumber) {
        return queryService.getByAwbNumber(awbNumber);
    }

    @GetMapping
    public List<ShipmentResponse> listByStatus(
            @RequestParam(name = "status", defaultValue = "CREATED") ShipmentStatus status,
            @RequestParam(name = "limit", defaultValue = "25") @Min(1) @Max(100) int limit
    ) {
        return queryService.listByStatus(status, limit);
    }

    @PostMapping("/{shipmentId}/manifest")
    public ShipmentResponse manifestShipment(@PathVariable("shipmentId") UUID shipmentId) {
        return commandService.manifestShipment(shipmentId);
    }

    @PostMapping("/{shipmentId}/dispatch")
    public ShipmentResponse dispatchShipment(@PathVariable("shipmentId") UUID shipmentId) {
        return commandService.dispatchShipment(shipmentId);
    }

    @PostMapping("/{shipmentId}/deliver")
    public ShipmentResponse markDelivered(@PathVariable("shipmentId") UUID shipmentId) {
        return commandService.markDelivered(shipmentId);
    }
}
