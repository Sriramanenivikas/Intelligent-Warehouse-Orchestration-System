package com.iwos.inventoryledger.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.inventoryledger.domain.idempotency.IdempotencyConflictException;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryCommandIdempotencyRecordEntity;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryCommandIdempotencyRecordRepository;
import com.iwos.inventoryledger.infrastructure.redis.CommandIdempotencyCache;
import com.iwos.inventoryledger.infrastructure.redis.CommandIdempotencyCacheEntry;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class InventoryCommandIdempotencyService {

    private final InventoryCommandIdempotencyRecordRepository recordRepository;
    private final CommandIdempotencyCache commandIdempotencyCache;
    private final ObjectMapper objectMapper;

    public InventoryCommandIdempotencyService(
            InventoryCommandIdempotencyRecordRepository recordRepository,
            CommandIdempotencyCache commandIdempotencyCache,
            ObjectMapper objectMapper
    ) {
        this.recordRepository = recordRepository;
        this.commandIdempotencyCache = commandIdempotencyCache;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<IdempotentCommandResult<T>> resolveReplay(
            String idempotencyKey,
            String operationType,
            String requestHash,
            Class<T> responseType
    ) {
        Optional<IdempotentCommandResult<T>> cacheReplay =
                resolveFromCache(idempotencyKey, operationType, requestHash, responseType);
        if (cacheReplay.isPresent()) {
            return cacheReplay;
        }

        return recordRepository.findByIdempotencyKey(idempotencyKey)
                .map(record -> {
                    validateRecord(record.getOperationType(), record.getRequestHash(), operationType, requestHash);
                    cacheImmediately(idempotencyKey, record.getOperationType(), record.getRequestHash(), record.getResponseBody());
                    return new IdempotentCommandResult<>(deserialize(record.getResponseBody(), responseType), true);
                });
    }

    public void storeSuccessfulResponse(
            String idempotencyKey,
            String operationType,
            String requestHash,
            UUID resourceId,
            HttpStatus httpStatus,
            Object response
    ) {
        String responseBody = serialize(response);

        InventoryCommandIdempotencyRecordEntity record = new InventoryCommandIdempotencyRecordEntity();
        record.setIdempotencyRecordId(UUID.randomUUID());
        record.setIdempotencyKey(idempotencyKey);
        record.setOperationType(operationType);
        record.setRequestHash(requestHash);
        record.setResourceId(resourceId);
        record.setResponseBody(responseBody);
        record.setHttpStatus(httpStatus.value());
        recordRepository.saveAndFlush(record);

        cacheAfterCommit(idempotencyKey, operationType, requestHash, responseBody);
    }

    private <T> Optional<IdempotentCommandResult<T>> resolveFromCache(
            String idempotencyKey,
            String operationType,
            String requestHash,
            Class<T> responseType
    ) {
        return commandIdempotencyCache.find(idempotencyKey)
                .map(entry -> {
                    validateEntry(entry, operationType, requestHash);
                    return new IdempotentCommandResult<>(deserialize(entry.responseBody(), responseType), true);
                });
    }

    private void validateEntry(CommandIdempotencyCacheEntry entry, String operationType, String requestHash) {
        validateRecord(entry.operationType(), entry.requestHash(), operationType, requestHash);
    }

    private void validateRecord(String existingOperationType, String existingRequestHash, String operationType, String requestHash) {
        if (!existingOperationType.equals(operationType) || !existingRequestHash.equals(requestHash)) {
            throw new IdempotencyConflictException("Idempotency key was already used with a different request payload");
        }
    }

    private void cacheAfterCommit(String idempotencyKey, String operationType, String requestHash, String responseBody) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cacheImmediately(idempotencyKey, operationType, requestHash, responseBody);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheImmediately(idempotencyKey, operationType, requestHash, responseBody);
            }
        });
    }

    private void cacheImmediately(String idempotencyKey, String operationType, String requestHash, String responseBody) {
        commandIdempotencyCache.put(idempotencyKey, operationType, requestHash, responseBody);
    }

    private String serialize(Object response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize command response", exception);
        }
    }

    private <T> T deserialize(String payload, Class<T> responseType) {
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize idempotent response", exception);
        }
    }
}
