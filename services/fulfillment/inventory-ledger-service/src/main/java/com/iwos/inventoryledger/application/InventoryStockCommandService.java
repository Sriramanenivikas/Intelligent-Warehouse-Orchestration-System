package com.iwos.inventoryledger.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.inventoryledger.api.http.StockAdjustmentRequest;
import com.iwos.inventoryledger.api.http.StockAdjustmentResponse;
import com.iwos.inventoryledger.domain.inventory.InventoryLedgerEntryType;
import com.iwos.inventoryledger.domain.inventory.InventoryStockSnapshot;
import com.iwos.inventoryledger.infrastructure.persistence.InventoryResponseMapper;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryLedgerEntryEntity;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryOutboxEventEntity;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryStockEntity;
import com.iwos.inventoryledger.infrastructure.persistence.jdbc.InventoryStockMutationStore;
import com.iwos.inventoryledger.infrastructure.observability.InventoryMetrics;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryLedgerEntryRepository;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryOutboxEventRepository;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryStockRepository;
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
public class InventoryStockCommandService {

    private static final String OPERATION_TYPE = "STOCK_ADJUSTMENT";
    private static final String EVENT_TYPE = "inventory.stock-adjusted.v1";

    private final InventoryCommandIdempotencyService inventoryCommandIdempotencyService;
    private final InventoryStockMutationStore inventoryStockMutationStore;
    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryLedgerEntryRepository inventoryLedgerEntryRepository;
    private final InventoryOutboxEventRepository inventoryOutboxEventRepository;
    private final InventoryResponseMapper inventoryResponseMapper;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;
    private final InventoryMetrics inventoryMetrics;

    public InventoryStockCommandService(
            InventoryCommandIdempotencyService inventoryCommandIdempotencyService,
            InventoryStockMutationStore inventoryStockMutationStore,
            InventoryStockRepository inventoryStockRepository,
            InventoryLedgerEntryRepository inventoryLedgerEntryRepository,
            InventoryOutboxEventRepository inventoryOutboxEventRepository,
            InventoryResponseMapper inventoryResponseMapper,
            RequestHashService requestHashService,
            ObjectMapper objectMapper,
            InventoryMetrics inventoryMetrics
    ) {
        this.inventoryCommandIdempotencyService = inventoryCommandIdempotencyService;
        this.inventoryStockMutationStore = inventoryStockMutationStore;
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryLedgerEntryRepository = inventoryLedgerEntryRepository;
        this.inventoryOutboxEventRepository = inventoryOutboxEventRepository;
        this.inventoryResponseMapper = inventoryResponseMapper;
        this.requestHashService = requestHashService;
        this.objectMapper = objectMapper;
        this.inventoryMetrics = inventoryMetrics;
    }

    @Transactional
    public IdempotentCommandResult<StockAdjustmentResponse> adjustStock(String idempotencyKey, StockAdjustmentRequest request) {
        var commandTimer = inventoryMetrics.startCommandTimer();
        try {
            String nodeId = normalize(request.nodeId());
            String sku = normalize(request.sku());
            StockAdjustmentRequest normalizedRequest = new StockAdjustmentRequest(
                    nodeId,
                    sku,
                    request.quantityDelta(),
                    request.reason().trim(),
                    trimToNull(request.referenceType()),
                    trimToNull(request.referenceId())
            );
            String requestHash = requestHashService.hash(Map.of("operationType", OPERATION_TYPE, "request", normalizedRequest));

            IdempotentCommandResult<StockAdjustmentResponse> result = inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, OPERATION_TYPE, requestHash, StockAdjustmentResponse.class)
                    .orElseGet(() -> createAdjustment(idempotencyKey, requestHash, normalizedRequest));

            inventoryMetrics.recordCommand("stock_adjustment", result.replayed() ? "replayed" : "accepted", commandTimer);
            return result;
        } catch (RuntimeException exception) {
            inventoryMetrics.recordCommand("stock_adjustment", "failed", commandTimer);
            throw exception;
        }
    }

    private IdempotentCommandResult<StockAdjustmentResponse> createAdjustment(
            String idempotencyKey,
            String requestHash,
            StockAdjustmentRequest request
    ) {
        try {
            InventoryStockSnapshot stockSnapshot =
                    inventoryStockMutationStore.adjustStock(request.nodeId(), request.sku(), request.quantityDelta());

            InventoryLedgerEntryEntity ledgerEntry = new InventoryLedgerEntryEntity();
            ledgerEntry.setLedgerEntryId(UUID.randomUUID());
            ledgerEntry.setNodeId(request.nodeId());
            ledgerEntry.setSku(request.sku());
            ledgerEntry.setEntryType(InventoryLedgerEntryType.STOCK_ADJUSTMENT);
            ledgerEntry.setOnHandDelta(request.quantityDelta());
            ledgerEntry.setReservedDelta(0);
            ledgerEntry.setReason(request.reason());
            ledgerEntry.setReferenceType(request.referenceType());
            ledgerEntry.setReferenceId(request.referenceId());
            InventoryLedgerEntryEntity persistedLedgerEntry = inventoryLedgerEntryRepository.saveAndFlush(ledgerEntry);

            InventoryStockEntity stockEntity = inventoryStockRepository.findByNodeIdAndSku(request.nodeId(), request.sku())
                    .orElseThrow();
            writeOutboxEvent(stockEntity.getStockItemId(), stockSnapshot);

            StockAdjustmentResponse response =
                    inventoryResponseMapper.toStockAdjustmentResponse(persistedLedgerEntry, stockSnapshot);
            inventoryCommandIdempotencyService.storeSuccessfulResponse(
                    idempotencyKey,
                    OPERATION_TYPE,
                    requestHash,
                    persistedLedgerEntry.getLedgerEntryId(),
                    HttpStatus.ACCEPTED,
                    response
            );
            return new IdempotentCommandResult<>(response, false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryCommandIdempotencyService
                    .resolveReplay(idempotencyKey, OPERATION_TYPE, requestHash, StockAdjustmentResponse.class)
                    .orElseThrow(() -> exception);
        }
    }

    private void writeOutboxEvent(UUID aggregateId, InventoryStockSnapshot stockSnapshot) {
        InventoryOutboxEventEntity outboxEvent = new InventoryOutboxEventEntity();
        outboxEvent.setOutboxEventId(UUID.randomUUID());
        outboxEvent.setAggregateType("INVENTORY_STOCK_ITEM");
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(EVENT_TYPE);
        outboxEvent.setStatus("PENDING");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID());
        payload.put("aggregateId", aggregateId);
        payload.put("eventType", EVENT_TYPE);
        payload.put("nodeId", stockSnapshot.nodeId());
        payload.put("sku", stockSnapshot.sku());
        payload.put("onHandQuantity", stockSnapshot.onHandQuantity());
        payload.put("reservedQuantity", stockSnapshot.reservedQuantity());
        payload.put("occurredAt", Instant.now());

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
}
