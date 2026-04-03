package com.iwos.inventoryledger.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.inventoryledger.api.http.CreateInventoryReservationRequest;
import com.iwos.inventoryledger.api.http.InventoryReservationResponse;
import com.iwos.inventoryledger.api.http.ReservationActionRequest;
import com.iwos.inventoryledger.domain.inventory.InventoryLedgerEntryType;
import com.iwos.inventoryledger.domain.inventory.InventoryStockSnapshot;
import com.iwos.inventoryledger.domain.reservation.InventoryReservationNotFoundException;
import com.iwos.inventoryledger.domain.reservation.InventoryReservationStatus;
import com.iwos.inventoryledger.domain.reservation.ReservationStateConflictException;
import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import com.iwos.inventoryledger.infrastructure.observability.InventoryMetrics;
import com.iwos.inventoryledger.infrastructure.persistence.InventoryResponseMapper;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryLedgerEntryEntity;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryOutboxEventEntity;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryReservationEntity;
import com.iwos.inventoryledger.infrastructure.persistence.jdbc.InventoryStockMutationStore;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryLedgerEntryRepository;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryOutboxEventRepository;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryReservationRepository;
import com.iwos.inventoryledger.shared.RequestHashService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationCommandService {

    private static final String CREATE_OPERATION_TYPE = "CREATE_RESERVATION";
    private static final String CONFIRM_OPERATION_TYPE = "CONFIRM_RESERVATION";
    private static final String RELEASE_OPERATION_TYPE = "RELEASE_RESERVATION";

    private final InventoryCommandIdempotencyService inventoryCommandIdempotencyService;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryStockMutationStore inventoryStockMutationStore;
    private final InventoryLedgerEntryRepository inventoryLedgerEntryRepository;
    private final InventoryOutboxEventRepository inventoryOutboxEventRepository;
    private final InventoryResponseMapper inventoryResponseMapper;
    private final InventoryLedgerServiceProperties inventoryLedgerServiceProperties;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;
    private final InventoryMetrics inventoryMetrics;

    public InventoryReservationCommandService(
            InventoryCommandIdempotencyService inventoryCommandIdempotencyService,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryStockMutationStore inventoryStockMutationStore,
            InventoryLedgerEntryRepository inventoryLedgerEntryRepository,
            InventoryOutboxEventRepository inventoryOutboxEventRepository,
            InventoryResponseMapper inventoryResponseMapper,
            InventoryLedgerServiceProperties inventoryLedgerServiceProperties,
            RequestHashService requestHashService,
            ObjectMapper objectMapper,
            InventoryMetrics inventoryMetrics
    ) {
        this.inventoryCommandIdempotencyService = inventoryCommandIdempotencyService;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryStockMutationStore = inventoryStockMutationStore;
        this.inventoryLedgerEntryRepository = inventoryLedgerEntryRepository;
        this.inventoryOutboxEventRepository = inventoryOutboxEventRepository;
        this.inventoryResponseMapper = inventoryResponseMapper;
        this.inventoryLedgerServiceProperties = inventoryLedgerServiceProperties;
        this.requestHashService = requestHashService;
        this.objectMapper = objectMapper;
        this.inventoryMetrics = inventoryMetrics;
    }

    @Transactional
    public IdempotentCommandResult<InventoryReservationResponse> createReservation(
            String idempotencyKey,
            CreateInventoryReservationRequest request
    ) {
        var commandTimer = inventoryMetrics.startCommandTimer();
        try {
            CreateInventoryReservationRequest normalizedRequest = new CreateInventoryReservationRequest(
                    request.orderReference().trim(),
                    normalize(request.nodeId()),
                    normalize(request.sku()),
                    request.quantity()
            );
            String requestHash =
                    requestHashService.hash(Map.of("operationType", CREATE_OPERATION_TYPE, "request", normalizedRequest));

            IdempotentCommandResult<InventoryReservationResponse> result = inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, CREATE_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseGet(() -> createReservationInternal(idempotencyKey, requestHash, normalizedRequest));

            inventoryMetrics.recordCommand("create_reservation", result.replayed() ? "replayed" : "accepted", commandTimer);
            return result;
        } catch (RuntimeException exception) {
            inventoryMetrics.recordCommand("create_reservation", "failed", commandTimer);
            throw exception;
        }
    }

    @Transactional
    public IdempotentCommandResult<InventoryReservationResponse> confirmReservation(
            String idempotencyKey,
            UUID reservationId,
            ReservationActionRequest request
    ) {
        var commandTimer = inventoryMetrics.startCommandTimer();
        try {
            String requestHash = requestHashService.hash(Map.of(
                    "operationType", CONFIRM_OPERATION_TYPE,
                    "reservationId", reservationId,
                    "request", request
            ));

            IdempotentCommandResult<InventoryReservationResponse> result = inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, CONFIRM_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseGet(() -> confirmReservationInternal(idempotencyKey, requestHash, reservationId, request));

            inventoryMetrics.recordCommand("confirm_reservation", result.replayed() ? "replayed" : "accepted", commandTimer);
            return result;
        } catch (RuntimeException exception) {
            inventoryMetrics.recordCommand("confirm_reservation", "failed", commandTimer);
            throw exception;
        }
    }

    @Transactional
    public IdempotentCommandResult<InventoryReservationResponse> releaseReservation(
            String idempotencyKey,
            UUID reservationId,
            ReservationActionRequest request
    ) {
        var commandTimer = inventoryMetrics.startCommandTimer();
        try {
            String requestHash = requestHashService.hash(Map.of(
                    "operationType", RELEASE_OPERATION_TYPE,
                    "reservationId", reservationId,
                    "request", request
            ));

            IdempotentCommandResult<InventoryReservationResponse> result = inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, RELEASE_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseGet(() -> releaseReservationInternal(idempotencyKey, requestHash, reservationId, request));

            inventoryMetrics.recordCommand("release_reservation", result.replayed() ? "replayed" : "accepted", commandTimer);
            return result;
        } catch (RuntimeException exception) {
            inventoryMetrics.recordCommand("release_reservation", "failed", commandTimer);
            throw exception;
        }
    }

    private IdempotentCommandResult<InventoryReservationResponse> createReservationInternal(
            String idempotencyKey,
            String requestHash,
            CreateInventoryReservationRequest request
    ) {
        try {
            InventoryStockSnapshot stockSnapshot =
                    inventoryStockMutationStore.reserve(request.nodeId(), request.sku(), request.quantity());

            InventoryReservationEntity reservation = new InventoryReservationEntity();
            reservation.setReservationId(UUID.randomUUID());
            reservation.setOrderReference(request.orderReference());
            reservation.setNodeId(request.nodeId());
            reservation.setSku(request.sku());
            reservation.setQuantity(request.quantity());
            reservation.setStatus(InventoryReservationStatus.RESERVED);
            reservation.setExpiresAt(Instant.now().plus(inventoryLedgerServiceProperties.getReservationTtl()));
            InventoryReservationEntity persistedReservation = inventoryReservationRepository.saveAndFlush(reservation);

            InventoryLedgerEntryEntity ledgerEntry = new InventoryLedgerEntryEntity();
            ledgerEntry.setLedgerEntryId(UUID.randomUUID());
            ledgerEntry.setNodeId(request.nodeId());
            ledgerEntry.setSku(request.sku());
            ledgerEntry.setEntryType(InventoryLedgerEntryType.RESERVATION_CREATED);
            ledgerEntry.setOnHandDelta(0);
            ledgerEntry.setReservedDelta(request.quantity());
            ledgerEntry.setReason("RESERVATION_CREATED");
            ledgerEntry.setReferenceType("ORDER_REFERENCE");
            ledgerEntry.setReferenceId(request.orderReference());
            ledgerEntry.setReservationId(persistedReservation.getReservationId());
            inventoryLedgerEntryRepository.save(ledgerEntry);

            writeOutboxEvent(
                    persistedReservation.getReservationId(),
                    "INVENTORY_RESERVATION",
                    "inventory.reservation-created.v1",
                    reservationPayload(persistedReservation)
            );

            InventoryReservationResponse response =
                    inventoryResponseMapper.toReservationResponse(persistedReservation, stockSnapshot);
            inventoryCommandIdempotencyService.storeSuccessfulResponse(
                    idempotencyKey,
                    CREATE_OPERATION_TYPE,
                    requestHash,
                    persistedReservation.getReservationId(),
                    HttpStatus.ACCEPTED,
                    response
            );
            return new IdempotentCommandResult<>(response, false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, CREATE_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseThrow(() -> exception);
        }
    }

    private IdempotentCommandResult<InventoryReservationResponse> confirmReservationInternal(
            String idempotencyKey,
            String requestHash,
            UUID reservationId,
            ReservationActionRequest request
    ) {
        try {
            InventoryReservationEntity reservation = inventoryReservationRepository.findByReservationIdForUpdate(reservationId)
                    .orElseThrow(() -> new InventoryReservationNotFoundException(reservationId));

            if (reservation.getStatus() == InventoryReservationStatus.RELEASED) {
                throw new ReservationStateConflictException(reservationId, "cannot be confirmed after release");
            }

            if (reservation.getStatus() == InventoryReservationStatus.CONFIRMED) {
                InventoryStockSnapshot currentStock = inventoryStockMutationStore.findRequired(reservation.getNodeId(), reservation.getSku());
                InventoryReservationResponse response = inventoryResponseMapper.toReservationResponse(reservation, currentStock);
                inventoryCommandIdempotencyService.storeSuccessfulResponse(
                        idempotencyKey,
                        CONFIRM_OPERATION_TYPE,
                        requestHash,
                        reservationId,
                        HttpStatus.OK,
                        response
                );
                return new IdempotentCommandResult<>(response, false);
            }

            InventoryStockSnapshot stockSnapshot =
                    inventoryStockMutationStore.confirm(reservation.getNodeId(), reservation.getSku(), reservation.getQuantity());

            reservation.setStatus(InventoryReservationStatus.CONFIRMED);
            InventoryReservationEntity persistedReservation = inventoryReservationRepository.saveAndFlush(reservation);

            InventoryLedgerEntryEntity ledgerEntry = new InventoryLedgerEntryEntity();
            ledgerEntry.setLedgerEntryId(UUID.randomUUID());
            ledgerEntry.setNodeId(reservation.getNodeId());
            ledgerEntry.setSku(reservation.getSku());
            ledgerEntry.setEntryType(InventoryLedgerEntryType.RESERVATION_CONFIRMED);
            ledgerEntry.setOnHandDelta(-reservation.getQuantity());
            ledgerEntry.setReservedDelta(-reservation.getQuantity());
            ledgerEntry.setReason(defaultReason(request.reason(), "RESERVATION_CONFIRMED"));
            ledgerEntry.setReferenceType(trimToNull(request.referenceType()));
            ledgerEntry.setReferenceId(trimToNull(request.referenceId()));
            ledgerEntry.setReservationId(reservationId);
            inventoryLedgerEntryRepository.save(ledgerEntry);

            writeOutboxEvent(
                    reservationId,
                    "INVENTORY_RESERVATION",
                    "inventory.reservation-confirmed.v1",
                    reservationPayload(persistedReservation)
            );

            InventoryReservationResponse response =
                    inventoryResponseMapper.toReservationResponse(persistedReservation, stockSnapshot);
            inventoryCommandIdempotencyService.storeSuccessfulResponse(
                    idempotencyKey,
                    CONFIRM_OPERATION_TYPE,
                    requestHash,
                    reservationId,
                    HttpStatus.OK,
                    response
            );
            return new IdempotentCommandResult<>(response, false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, CONFIRM_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseThrow(() -> exception);
        }
    }

    private IdempotentCommandResult<InventoryReservationResponse> releaseReservationInternal(
            String idempotencyKey,
            String requestHash,
            UUID reservationId,
            ReservationActionRequest request
    ) {
        try {
            InventoryReservationEntity reservation = inventoryReservationRepository.findByReservationIdForUpdate(reservationId)
                    .orElseThrow(() -> new InventoryReservationNotFoundException(reservationId));

            if (reservation.getStatus() == InventoryReservationStatus.CONFIRMED) {
                throw new ReservationStateConflictException(reservationId, "cannot be released after confirmation");
            }

            if (reservation.getStatus() == InventoryReservationStatus.RELEASED) {
                InventoryStockSnapshot currentStock = inventoryStockMutationStore.findRequired(reservation.getNodeId(), reservation.getSku());
                InventoryReservationResponse response = inventoryResponseMapper.toReservationResponse(reservation, currentStock);
                inventoryCommandIdempotencyService.storeSuccessfulResponse(
                        idempotencyKey,
                        RELEASE_OPERATION_TYPE,
                        requestHash,
                        reservationId,
                        HttpStatus.OK,
                        response
                );
                return new IdempotentCommandResult<>(response, false);
            }

            InventoryStockSnapshot stockSnapshot =
                    inventoryStockMutationStore.release(reservation.getNodeId(), reservation.getSku(), reservation.getQuantity());

            reservation.setStatus(InventoryReservationStatus.RELEASED);
            InventoryReservationEntity persistedReservation = inventoryReservationRepository.saveAndFlush(reservation);

            InventoryLedgerEntryEntity ledgerEntry = new InventoryLedgerEntryEntity();
            ledgerEntry.setLedgerEntryId(UUID.randomUUID());
            ledgerEntry.setNodeId(reservation.getNodeId());
            ledgerEntry.setSku(reservation.getSku());
            ledgerEntry.setEntryType(InventoryLedgerEntryType.RESERVATION_RELEASED);
            ledgerEntry.setOnHandDelta(0);
            ledgerEntry.setReservedDelta(-reservation.getQuantity());
            ledgerEntry.setReason(defaultReason(request.reason(), "RESERVATION_RELEASED"));
            ledgerEntry.setReferenceType(trimToNull(request.referenceType()));
            ledgerEntry.setReferenceId(trimToNull(request.referenceId()));
            ledgerEntry.setReservationId(reservationId);
            inventoryLedgerEntryRepository.save(ledgerEntry);

            writeOutboxEvent(
                    reservationId,
                    "INVENTORY_RESERVATION",
                    "inventory.reservation-released.v1",
                    reservationPayload(persistedReservation)
            );

            InventoryReservationResponse response =
                    inventoryResponseMapper.toReservationResponse(persistedReservation, stockSnapshot);
            inventoryCommandIdempotencyService.storeSuccessfulResponse(
                    idempotencyKey,
                    RELEASE_OPERATION_TYPE,
                    requestHash,
                    reservationId,
                    HttpStatus.OK,
                    response
            );
            return new IdempotentCommandResult<>(response, false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, RELEASE_OPERATION_TYPE, requestHash, InventoryReservationResponse.class)
                    .orElseThrow(() -> exception);
        }
    }

    private Map<String, Object> reservationPayload(InventoryReservationEntity reservation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID());
        payload.put("reservationId", reservation.getReservationId());
        payload.put("orderReference", reservation.getOrderReference());
        payload.put("nodeId", reservation.getNodeId());
        payload.put("sku", reservation.getSku());
        payload.put("quantity", reservation.getQuantity());
        payload.put("status", reservation.getStatus());
        payload.put("occurredAt", Instant.now());
        return payload;
    }

    private void writeOutboxEvent(UUID aggregateId, String aggregateType, String eventType, Map<String, Object> payload) {
        InventoryOutboxEventEntity outboxEvent = new InventoryOutboxEventEntity();
        outboxEvent.setOutboxEventId(UUID.randomUUID());
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(eventType);
        outboxEvent.setStatus("PENDING");
        outboxEvent.setPayload(writeJson(payload));
        inventoryOutboxEventRepository.save(outboxEvent);
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize inventory outbox payload", exception);
        }
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String defaultReason(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }
}
