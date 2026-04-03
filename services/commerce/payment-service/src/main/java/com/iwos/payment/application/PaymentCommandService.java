package com.iwos.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.payment.api.http.CreatePaymentIntentRequest;
import com.iwos.payment.api.http.PaymentActionRequest;
import com.iwos.payment.api.http.PaymentIntentResponse;
import com.iwos.payment.domain.idempotency.IdempotencyConflictException;
import com.iwos.payment.domain.payment.PaymentIntentNotFoundException;
import com.iwos.payment.domain.payment.PaymentIntentStatus;
import com.iwos.payment.domain.payment.PaymentStateConflictException;
import com.iwos.payment.infrastructure.config.PaymentServiceProperties;
import com.iwos.payment.infrastructure.observability.PaymentMetrics;
import com.iwos.payment.infrastructure.persistence.PaymentIntentResponseMapper;
import com.iwos.payment.infrastructure.persistence.entity.PaymentIdempotencyRecordEntity;
import com.iwos.payment.infrastructure.persistence.entity.PaymentIntentEntity;
import com.iwos.payment.infrastructure.persistence.entity.PaymentOutboxEventEntity;
import com.iwos.payment.infrastructure.persistence.repository.PaymentIdempotencyRecordRepository;
import com.iwos.payment.infrastructure.persistence.repository.PaymentIntentRepository;
import com.iwos.payment.infrastructure.persistence.repository.PaymentOutboxEventRepository;
import com.iwos.payment.infrastructure.redis.IdempotencyCache;
import com.iwos.payment.shared.RequestHashService;
import java.math.BigDecimal;
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

@Service
public class PaymentCommandService {

    private static final String AUTHORIZE_OPERATION = "AUTHORIZE_PAYMENT";
    private static final String SUCCESS_OPERATION = "MARK_PAYMENT_SUCCESS";
    private static final String FAIL_OPERATION = "MARK_PAYMENT_FAIL";

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentIdempotencyRecordRepository idempotencyRecordRepository;
    private final PaymentOutboxEventRepository outboxEventRepository;
    private final PaymentIntentResponseMapper responseMapper;
    private final IdempotencyCache idempotencyCache;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;
    private final PaymentMetrics paymentMetrics;
    private final PaymentServiceProperties properties;

    public PaymentCommandService(
            PaymentIntentRepository paymentIntentRepository,
            PaymentIdempotencyRecordRepository idempotencyRecordRepository,
            PaymentOutboxEventRepository outboxEventRepository,
            PaymentIntentResponseMapper responseMapper,
            IdempotencyCache idempotencyCache,
            RequestHashService requestHashService,
            ObjectMapper objectMapper,
            PaymentMetrics paymentMetrics,
            PaymentServiceProperties properties
    ) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.responseMapper = responseMapper;
        this.idempotencyCache = idempotencyCache;
        this.requestHashService = requestHashService;
        this.objectMapper = objectMapper;
        this.paymentMetrics = paymentMetrics;
        this.properties = properties;
    }

    @Transactional
    public PaymentCommandResult<PaymentIntentResponse> authorize(String idempotencyKey, CreatePaymentIntentRequest request) {
        var timer = paymentMetrics.startCommandTimer();
        try {
            CreatePaymentIntentRequest normalizedRequest = normalize(request);
            String requestHash = requestHashService.hash(Map.of(
                    "operationType", AUTHORIZE_OPERATION,
                    "request", normalizedRequest
            ));

            Optional<PaymentCommandResult<PaymentIntentResponse>> cachedReplay = resolveFromCache(idempotencyKey, AUTHORIZE_OPERATION, requestHash);
            if (cachedReplay.isPresent()) {
                paymentMetrics.recordCommand("authorize", "replayed", timer);
                return cachedReplay.get();
            }

            Optional<PaymentCommandResult<PaymentIntentResponse>> persistedReplay = resolveFromPersistence(
                    idempotencyKey,
                    AUTHORIZE_OPERATION,
                    requestHash,
                    normalizedRequest.orderIntentId()
            );
            if (persistedReplay.isPresent()) {
                paymentMetrics.recordCommand("authorize", "replayed", timer);
                return persistedReplay.get();
            }

            try {
                PaymentCommandResult<PaymentIntentResponse> result = createNewPaymentIntent(idempotencyKey, requestHash, normalizedRequest);
                paymentMetrics.recordCommand("authorize", "authorized", timer);
                return result;
            } catch (DataIntegrityViolationException exception) {
                PaymentCommandResult<PaymentIntentResponse> result = resolveFromPersistence(
                        idempotencyKey,
                        AUTHORIZE_OPERATION,
                        requestHash,
                        normalizedRequest.orderIntentId(),
                        normalizedRequest.orderIntentId()
                ).orElseThrow(() -> exception);
                paymentMetrics.recordCommand("authorize", "replayed", timer);
                return result;
            }
        } catch (RuntimeException exception) {
            paymentMetrics.recordCommand("authorize", "failed", timer);
            throw exception;
        }
    }

    @Transactional
    public PaymentCommandResult<PaymentIntentResponse> succeed(String idempotencyKey, UUID paymentIntentId, PaymentActionRequest request) {
        var timer = paymentMetrics.startCommandTimer();
        try {
            String requestHash = requestHashService.hash(Map.of(
                    "operationType", SUCCESS_OPERATION,
                    "paymentIntentId", paymentIntentId,
                    "request", normalize(request)
            ));

            Optional<PaymentCommandResult<PaymentIntentResponse>> cachedReplay = resolveFromCache(idempotencyKey, SUCCESS_OPERATION, requestHash);
            if (cachedReplay.isPresent()) {
                paymentMetrics.recordCommand("succeed", "replayed", timer);
                return cachedReplay.get();
            }

            Optional<PaymentCommandResult<PaymentIntentResponse>> persistedReplay = resolveFromPersistence(
                    idempotencyKey,
                    SUCCESS_OPERATION,
                    requestHash,
                    paymentIntentId
            );
            if (persistedReplay.isPresent()) {
                paymentMetrics.recordCommand("succeed", "replayed", timer);
                return persistedReplay.get();
            }

            PaymentCommandResult<PaymentIntentResponse> result = applyTerminalState(
                    idempotencyKey,
                    SUCCESS_OPERATION,
                    requestHash,
                    paymentIntentId,
                    PaymentIntentStatus.SUCCEEDED,
                    request
            );
            paymentMetrics.recordCommand("succeed", "succeeded", timer);
            return result;
        } catch (RuntimeException exception) {
            paymentMetrics.recordCommand("succeed", "failed", timer);
            throw exception;
        }
    }

    @Transactional
    public PaymentCommandResult<PaymentIntentResponse> fail(String idempotencyKey, UUID paymentIntentId, PaymentActionRequest request) {
        var timer = paymentMetrics.startCommandTimer();
        try {
            String requestHash = requestHashService.hash(Map.of(
                    "operationType", FAIL_OPERATION,
                    "paymentIntentId", paymentIntentId,
                    "request", normalize(request)
            ));

            Optional<PaymentCommandResult<PaymentIntentResponse>> cachedReplay = resolveFromCache(idempotencyKey, FAIL_OPERATION, requestHash);
            if (cachedReplay.isPresent()) {
                paymentMetrics.recordCommand("fail", "replayed", timer);
                return cachedReplay.get();
            }

            Optional<PaymentCommandResult<PaymentIntentResponse>> persistedReplay = resolveFromPersistence(
                    idempotencyKey,
                    FAIL_OPERATION,
                    requestHash,
                    paymentIntentId
            );
            if (persistedReplay.isPresent()) {
                paymentMetrics.recordCommand("fail", "replayed", timer);
                return persistedReplay.get();
            }

            PaymentCommandResult<PaymentIntentResponse> result = applyTerminalState(
                    idempotencyKey,
                    FAIL_OPERATION,
                    requestHash,
                    paymentIntentId,
                    PaymentIntentStatus.FAILED,
                    request
            );
            paymentMetrics.recordCommand("fail", "failed", timer);
            return result;
        } catch (RuntimeException exception) {
            paymentMetrics.recordCommand("fail", "failed", timer);
            throw exception;
        }
    }

    private Optional<PaymentCommandResult<PaymentIntentResponse>> resolveFromCache(
            String idempotencyKey,
            String operationType,
            String requestHash
    ) {
        return idempotencyCache.find(cacheKey(idempotencyKey, operationType))
                .map(entry -> {
                    validateHash(entry.requestHash(), requestHash);
                    return new PaymentCommandResult<>(entry.response(), true);
                });
    }

    private Optional<PaymentCommandResult<PaymentIntentResponse>> resolveFromPersistence(
            String idempotencyKey,
            String operationType,
            String requestHash,
            UUID paymentIntentId
    ) {
        return idempotencyRecordRepository.findByIdempotencyKeyAndOperationType(idempotencyKey, operationType)
                .map(record -> {
                    validateHash(record.getRequestHash(), requestHash);
                    PaymentIntentResponse response = deserializeResponse(record.getResponseBody());
                    cacheImmediately(idempotencyKey, operationType, requestHash, response);
                    return new PaymentCommandResult<>(response, true);
                });
    }

    private Optional<PaymentCommandResult<PaymentIntentResponse>> resolveFromPersistence(
            String idempotencyKey,
            String operationType,
            String requestHash,
            UUID paymentIntentId,
            UUID orderIntentId
    ) {
        return resolveFromPersistence(idempotencyKey, operationType, requestHash, paymentIntentId)
                .or(() -> paymentIntentRepository.findByOrderIntentId(orderIntentId)
                        .map(entity -> {
                            PaymentIntentResponse response = responseMapper.toResponse(entity);
                            cacheImmediately(idempotencyKey, operationType, requestHash, response);
                            return new PaymentCommandResult<>(response, true);
                        }));
    }

    private PaymentCommandResult<PaymentIntentResponse> createNewPaymentIntent(
            String idempotencyKey,
            String requestHash,
            CreatePaymentIntentRequest request
    ) {
        PaymentIntentEntity paymentIntent = new PaymentIntentEntity();
        paymentIntent.setPaymentIntentId(UUID.randomUUID());
        paymentIntent.setOrderIntentId(request.orderIntentId());
        paymentIntent.setOrderWorkflowId(request.orderWorkflowId());
        paymentIntent.setCustomerId(request.customerId().trim());
        paymentIntent.setPaymentMode(request.paymentMode().trim().toUpperCase());
        paymentIntent.setCurrency(request.currency().trim().toUpperCase());
        paymentIntent.setTotalAmount(normalizeAmount(request.totalAmount()));
        paymentIntent.setCapturedAmount(BigDecimal.ZERO);
        paymentIntent.setProviderName(properties.getProviderName());
        paymentIntent.setProviderReference("PAY-" + paymentIntent.getPaymentIntentId());

        Instant processedAt = Instant.now();
        boolean authorizationApproved = request.totalAmount().compareTo(properties.getAuthorizationLimit()) <= 0;
        if (authorizationApproved) {
            paymentIntent.setStatus(PaymentIntentStatus.AUTHORIZED);
            paymentIntent.setAuthorizedAt(processedAt);
            paymentIntent.setFailureReason(null);
        } else {
            paymentIntent.setStatus(PaymentIntentStatus.FAILED);
            paymentIntent.setFailedAt(processedAt);
            paymentIntent.setFailureReason("Authorization declined by simulated gateway threshold");
        }

        PaymentIntentEntity persistedIntent = paymentIntentRepository.saveAndFlush(paymentIntent);
        PaymentIntentResponse response = responseMapper.toResponse(persistedIntent);

        writeOutboxEvent(
                persistedIntent.getPaymentIntentId(),
                "PAYMENT_INTENT",
                authorizationApproved ? "payment.intent-authorized.v1" : "payment.intent-failed.v1",
                authorizationPayload(persistedIntent, authorizationApproved)
        );

        storeIdempotencyRecord(idempotencyKey, AUTHORIZE_OPERATION, requestHash, persistedIntent.getPaymentIntentId(), HttpStatus.ACCEPTED, response);
        cacheAfterCommit(idempotencyKey, AUTHORIZE_OPERATION, requestHash, response);
        return new PaymentCommandResult<>(response, false);
    }

    private PaymentCommandResult<PaymentIntentResponse> applyTerminalState(
            String idempotencyKey,
            String operationType,
            String requestHash,
            UUID paymentIntentId,
            PaymentIntentStatus terminalStatus,
            PaymentActionRequest request
    ) {
        PaymentIntentEntity paymentIntent = paymentIntentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        if (paymentIntent.getStatus() == PaymentIntentStatus.SUCCEEDED && terminalStatus == PaymentIntentStatus.SUCCEEDED) {
            PaymentIntentResponse response = responseMapper.toResponse(paymentIntent);
            cacheAfterCommit(idempotencyKey, operationType, requestHash, response);
            storeIdempotencyRecord(idempotencyKey, operationType, requestHash, paymentIntentId, HttpStatus.OK, response);
            return new PaymentCommandResult<>(response, true);
        }

        if (paymentIntent.getStatus() == PaymentIntentStatus.FAILED && terminalStatus == PaymentIntentStatus.FAILED) {
            PaymentIntentResponse response = responseMapper.toResponse(paymentIntent);
            cacheAfterCommit(idempotencyKey, operationType, requestHash, response);
            storeIdempotencyRecord(idempotencyKey, operationType, requestHash, paymentIntentId, HttpStatus.OK, response);
            return new PaymentCommandResult<>(response, true);
        }

        if (paymentIntent.getStatus() == PaymentIntentStatus.FAILED && terminalStatus == PaymentIntentStatus.SUCCEEDED) {
            throw new PaymentStateConflictException(paymentIntentId, "cannot be marked successful after failure");
        }

        if (paymentIntent.getStatus() == PaymentIntentStatus.SUCCEEDED && terminalStatus == PaymentIntentStatus.FAILED) {
            throw new PaymentStateConflictException(paymentIntentId, "cannot be marked failed after success");
        }

        if (terminalStatus == PaymentIntentStatus.SUCCEEDED && paymentIntent.getStatus() != PaymentIntentStatus.AUTHORIZED) {
            throw new PaymentStateConflictException(paymentIntentId, "must be authorized before success");
        }

        if (terminalStatus == PaymentIntentStatus.FAILED && paymentIntent.getStatus() != PaymentIntentStatus.AUTHORIZED) {
            throw new PaymentStateConflictException(paymentIntentId, "must be authorized before failure");
        }

        if (terminalStatus == PaymentIntentStatus.SUCCEEDED) {
            paymentIntent.setStatus(PaymentIntentStatus.SUCCEEDED);
            paymentIntent.setCapturedAmount(paymentIntent.getTotalAmount());
            paymentIntent.setSucceededAt(Instant.now());
            paymentIntent.setFailureReason(null);
        } else {
            paymentIntent.setStatus(PaymentIntentStatus.FAILED);
            paymentIntent.setFailedAt(Instant.now());
            paymentIntent.setFailureReason(normalizeReason(request.reason()));
        }

        PaymentIntentEntity persistedIntent = paymentIntentRepository.saveAndFlush(paymentIntent);
        PaymentIntentResponse response = responseMapper.toResponse(persistedIntent);

        writeOutboxEvent(
                persistedIntent.getPaymentIntentId(),
                "PAYMENT_INTENT",
                terminalStatus == PaymentIntentStatus.SUCCEEDED ? "payment.intent-succeeded.v1" : "payment.intent-failed.v1",
                Map.of(
                        "eventId", UUID.randomUUID(),
                        "paymentIntentId", persistedIntent.getPaymentIntentId(),
                        "orderIntentId", persistedIntent.getOrderIntentId(),
                        "orderWorkflowId", persistedIntent.getOrderWorkflowId(),
                        "status", persistedIntent.getStatus().name(),
                        "occurredAt", terminalStatus == PaymentIntentStatus.SUCCEEDED
                                ? persistedIntent.getSucceededAt()
                                : persistedIntent.getFailedAt()
                )
        );

        storeIdempotencyRecord(idempotencyKey, operationType, requestHash, persistedIntent.getPaymentIntentId(), HttpStatus.OK, response);
        cacheAfterCommit(idempotencyKey, operationType, requestHash, response);
        return new PaymentCommandResult<>(response, false);
    }

    private void storeIdempotencyRecord(
            String idempotencyKey,
            String operationType,
            String requestHash,
            UUID paymentIntentId,
            HttpStatus httpStatus,
            PaymentIntentResponse response
    ) {
        PaymentIdempotencyRecordEntity record = new PaymentIdempotencyRecordEntity();
        record.setIdempotencyRecordId(UUID.randomUUID());
        record.setIdempotencyKey(idempotencyKey);
        record.setOperationType(operationType);
        record.setRequestHash(requestHash);
        record.setPaymentIntentId(paymentIntentId);
        record.setHttpStatus(httpStatus.value());
        record.setResponseBody(writeJson(response));
        idempotencyRecordRepository.saveAndFlush(record);
    }

    private void writeOutboxEvent(UUID paymentIntentId, String aggregateType, String eventType, Map<String, Object> payload) {
        PaymentOutboxEventEntity event = new PaymentOutboxEventEntity();
        event.setOutboxEventId(UUID.randomUUID());
        event.setAggregateType(aggregateType);
        event.setAggregateId(paymentIntentId);
        event.setEventType(eventType);
        event.setStatus("PENDING");
        event.setAttempts(0);
        event.setPayload(writeJson(payload));
        outboxEventRepository.save(event);
    }

    private void validateHash(String existingHash, String currentHash) {
        if (!existingHash.equals(currentHash)) {
            throw new IdempotencyConflictException("Idempotency key was already used with a different request payload");
        }
    }

    private PaymentIntentResponse deserializeResponse(String payload) {
        try {
            return objectMapper.readValue(payload, PaymentIntentResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize payment response", exception);
        }
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payment payload", exception);
        }
    }

    private void cacheImmediately(String idempotencyKey, String operationType, String requestHash, PaymentIntentResponse response) {
        idempotencyCache.put(cacheKey(idempotencyKey, operationType), requestHash, response);
    }

    private Map<String, Object> authorizationPayload(PaymentIntentEntity paymentIntent, boolean authorizationApproved) {
        java.util.LinkedHashMap<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID());
        payload.put("paymentIntentId", paymentIntent.getPaymentIntentId());
        payload.put("orderIntentId", paymentIntent.getOrderIntentId());
        payload.put("orderWorkflowId", paymentIntent.getOrderWorkflowId());
        payload.put("status", paymentIntent.getStatus().name());
        payload.put("occurredAt", authorizationApproved ? paymentIntent.getAuthorizedAt() : paymentIntent.getFailedAt());
        if (paymentIntent.getFailureReason() != null) {
            payload.put("failureReason", paymentIntent.getFailureReason());
        }
        return payload;
    }

    private void cacheAfterCommit(String idempotencyKey, String operationType, String requestHash, PaymentIntentResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cacheImmediately(idempotencyKey, operationType, requestHash, response);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheImmediately(idempotencyKey, operationType, requestHash, response);
            }
        });
    }

    private String cacheKey(String idempotencyKey, String operationType) {
        return idempotencyKey + ":" + operationType;
    }

    private CreatePaymentIntentRequest normalize(CreatePaymentIntentRequest request) {
        return new CreatePaymentIntentRequest(
                request.orderIntentId(),
                request.orderWorkflowId(),
                request.customerId().trim(),
                request.paymentMode().trim().toUpperCase(),
                request.currency().trim().toUpperCase(),
                normalizeAmount(request.totalAmount())
        );
    }

    private PaymentActionRequest normalize(PaymentActionRequest request) {
        if (request == null) {
            return new PaymentActionRequest(null, null, null);
        }
        return new PaymentActionRequest(
                normalizeReason(request.reason()),
                trimToNull(request.referenceType()),
                trimToNull(request.referenceId())
        );
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        return value.setScale(2);
    }

    private String normalizeReason(String reason) {
        return trimToNull(reason);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
