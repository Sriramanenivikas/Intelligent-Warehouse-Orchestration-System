package com.iwos.inventoryledger.api.http;

import com.iwos.inventoryledger.application.IdempotentCommandResult;
import com.iwos.inventoryledger.application.InventoryQueryService;
import com.iwos.inventoryledger.application.InventoryReservationCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/reservations")
public class InventoryReservationController {

    private final InventoryReservationCommandService inventoryReservationCommandService;
    private final InventoryQueryService inventoryQueryService;

    public InventoryReservationController(
            InventoryReservationCommandService inventoryReservationCommandService,
            InventoryQueryService inventoryQueryService
    ) {
        this.inventoryReservationCommandService = inventoryReservationCommandService;
        this.inventoryQueryService = inventoryQueryService;
    }

    @PostMapping
    public ResponseEntity<InventoryReservationResponse> createReservation(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreateInventoryReservationRequest request
    ) {
        IdempotentCommandResult<InventoryReservationResponse> result =
                inventoryReservationCommandService.createReservation(idempotencyKey, request);
        return ResponseEntity.accepted()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<InventoryReservationResponse> confirmReservation(
            @PathVariable("reservationId") UUID reservationId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody(required = false) ReservationActionRequest request
    ) {
        IdempotentCommandResult<InventoryReservationResponse> result =
                inventoryReservationCommandService.confirmReservation(idempotencyKey, reservationId, emptyIfNull(request));
        return ResponseEntity.ok()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @PostMapping("/{reservationId}/release")
    public ResponseEntity<InventoryReservationResponse> releaseReservation(
            @PathVariable("reservationId") UUID reservationId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody(required = false) ReservationActionRequest request
    ) {
        IdempotentCommandResult<InventoryReservationResponse> result =
                inventoryReservationCommandService.releaseReservation(idempotencyKey, reservationId, emptyIfNull(request));
        return ResponseEntity.ok()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @GetMapping("/{reservationId}")
    public InventoryReservationResponse getReservation(@PathVariable("reservationId") UUID reservationId) {
        return inventoryQueryService.getReservation(reservationId);
    }

    private ReservationActionRequest emptyIfNull(ReservationActionRequest request) {
        if (request == null) {
            return new ReservationActionRequest(null, null, null);
        }
        return request;
    }
}
