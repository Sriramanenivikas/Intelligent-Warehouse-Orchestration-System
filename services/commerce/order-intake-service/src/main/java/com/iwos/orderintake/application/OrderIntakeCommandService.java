package com.iwos.orderintake.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderintake.api.http.CreateOrderIntentRequest;
import com.iwos.orderintake.api.http.OrderIntentResponse;
import com.iwos.orderintake.domain.idempotency.IdempotencyConflictException;
import com.iwos.orderintake.domain.order.OrderIntentStatus;
import com.iwos.orderintake.infrastructure.persistence.OrderIntentResponseMapper;
import com.iwos.orderintake.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.iwos.orderintake.infrastructure.persistence.entity.OrderIntentEntity;
import com.iwos.orderintake.infrastructure.persistence.entity.OrderIntentItemEntity;
import com.iwos.orderintake.infrastructure.persistence.entity.OutboxEventEntity;
import com.iwos.orderintake.infrastructure.persistence.repository.IdempotencyRecordRepository;
import com.iwos.orderintake.infrastructure.persistence.repository.OrderIntentRepository;
import com.iwos.orderintake.infrastructure.persistence.repository.OutboxEventRepository;
import com.iwos.orderintake.infrastructure.redis.IdempotencyCache;
import com.iwos.orderintake.infrastructure.redis.IdempotencyCacheEntry;
import com.iwos.orderintake.shared.RequestHashService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Accepts new order intents and enforces idempotency semantics.
 */
@Service
public class OrderIntakeCommandService {

    private final OrderIntentRepository orderIntentRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderIntentResponseMapper responseMapper;
    private final IdempotencyCache idempotencyCache;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;

    public OrderIntakeCommandService(
            OrderIntentRepository orderIntentRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            OutboxEventRepository outboxEventRepository,
            OrderIntentResponseMapper responseMapper,
            IdempotencyCache idempotencyCache,
            RequestHashService requestHashService,
            ObjectMapper objectMapper
    ) {
        this.orderIntentRepository = orderIntentRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.responseMapper = responseMapper;
        this.idempotencyCache = idempotencyCache;
        this.requestHashService = requestHashService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderIntentAcceptedResult accept(String idempotencyKey, CreateOrderIntentRequest request) {
        String requestHash = requestHashService.hash(request);

        Optional<OrderIntentAcceptedResult> cachedReplay = resolveFromCache(idempotencyKey, requestHash);
        if (cachedReplay.isPresent()) {
            return cachedReplay.get();
        }

        Optional<OrderIntentAcceptedResult> persistedReplay = resolveFromPersistence(idempotencyKey, requestHash);
        if (persistedReplay.isPresent()) {
            return persistedReplay.get();
        }

        try {
            return createNewOrderIntent(idempotencyKey, requestHash, request);
        } catch (DataIntegrityViolationException exception) {
            return resolveFromPersistence(idempotencyKey, requestHash)
                    .orElseThrow(() -> exception);
        }
    }

    private Optional<OrderIntentAcceptedResult> resolveFromCache(String idempotencyKey, String requestHash) {
        return idempotencyCache.find(idempotencyKey)
                .map(entry -> {
                    validateHash(entry.requestHash(), requestHash);
                    return new OrderIntentAcceptedResult(entry.response(), true);
                });
    }

    private Optional<OrderIntentAcceptedResult> resolveFromPersistence(String idempotencyKey, String requestHash) {
        return idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .map(record -> {
                    validateHash(record.getRequestHash(), requestHash);
                    OrderIntentResponse response = deserializeResponse(record.getResponseBody());
                    cacheImmediately(idempotencyKey, requestHash, response);
                    return new OrderIntentAcceptedResult(response, true);
                });
    }

    private OrderIntentAcceptedResult createNewOrderIntent(
            String idempotencyKey,
            String requestHash,
            CreateOrderIntentRequest request
    ) {
        OrderIntentEntity orderIntent = new OrderIntentEntity();
        orderIntent.setOrderIntentId(UUID.randomUUID());
        orderIntent.setCustomerId(request.customerId());
        orderIntent.setChannel(request.channel());
        orderIntent.setPaymentMode(request.paymentMode());
        orderIntent.setCurrency(request.currency().trim().toUpperCase());
        orderIntent.setDeliveryAddressJson(writeJson(request.deliveryAddress()));
        orderIntent.setStatus(OrderIntentStatus.ACCEPTED);
        orderIntent.setRequestHash(requestHash);
        orderIntent.setAcceptedAt(Instant.now());

        request.items().forEach(item -> {
            OrderIntentItemEntity itemEntity = new OrderIntentItemEntity();
            itemEntity.setOrderIntentItemId(UUID.randomUUID());
            itemEntity.setSku(item.sku());
            itemEntity.setQuantity(item.quantity());
            orderIntent.addItem(itemEntity);
        });

        OrderIntentEntity persistedOrderIntent = orderIntentRepository.save(orderIntent);
        OrderIntentResponse response = responseMapper.toResponse(persistedOrderIntent);

        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setOutboxEventId(UUID.randomUUID());
        outboxEvent.setAggregateType("ORDER_INTENT");
        outboxEvent.setAggregateId(persistedOrderIntent.getOrderIntentId());
        outboxEvent.setEventType("order-intake.accepted.v1");
        outboxEvent.setStatus("PENDING");
        outboxEvent.setPayload(writeJson(Map.of(
                "eventId", UUID.randomUUID(),
                "aggregateId", persistedOrderIntent.getOrderIntentId(),
                "eventType", "order-intake.accepted.v1",
                "occurredAt", persistedOrderIntent.getAcceptedAt()
        )));
        outboxEventRepository.save(outboxEvent);

        IdempotencyRecordEntity idempotencyRecord = new IdempotencyRecordEntity();
        idempotencyRecord.setIdempotencyRecordId(UUID.randomUUID());
        idempotencyRecord.setIdempotencyKey(idempotencyKey);
        idempotencyRecord.setRequestHash(requestHash);
        idempotencyRecord.setOrderIntentId(persistedOrderIntent.getOrderIntentId());
        idempotencyRecord.setResponseBody(writeJson(response));
        idempotencyRecord.setHttpStatus(HttpStatus.ACCEPTED.value());
        idempotencyRecordRepository.saveAndFlush(idempotencyRecord);

        cacheAfterCommit(idempotencyKey, requestHash, response);
        return new OrderIntentAcceptedResult(response, false);
    }

    private void validateHash(String existingHash, String currentHash) {
        if (!existingHash.equals(currentHash)) {
            throw new IdempotencyConflictException("Idempotency key was already used with a different request payload");
        }
    }

    private OrderIntentResponse deserializeResponse(String payload) {
        try {
            return objectMapper.readValue(payload, OrderIntentResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize idempotent response", exception);
        }
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payload", exception);
        }
    }

    private void cacheImmediately(String idempotencyKey, String requestHash, OrderIntentResponse response) {
        idempotencyCache.put(idempotencyKey, requestHash, response);
    }

    private void cacheAfterCommit(String idempotencyKey, String requestHash, OrderIntentResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cacheImmediately(idempotencyKey, requestHash, response);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheImmediately(idempotencyKey, requestHash, response);
            }
        });
    }
}
