package com.iwos.orderorchestrator.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderorchestrator.api.http.OrderWorkflowResponse;
import com.iwos.orderorchestrator.api.http.ProcessPendingWorkflowsResponse;
import com.iwos.orderorchestrator.domain.workflow.OrderIntentSourceEventNotFoundException;
import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowReservationStatus;
import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowStatus;
import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import com.iwos.orderorchestrator.infrastructure.inventory.InventoryCreateReservationRequest;
import com.iwos.orderorchestrator.infrastructure.inventory.InventoryReservationActionRequest;
import com.iwos.orderorchestrator.infrastructure.inventory.InventoryReservationClientResponse;
import com.iwos.orderorchestrator.infrastructure.inventory.InventoryServiceClient;
import com.iwos.orderorchestrator.infrastructure.persistence.OrderWorkflowResponseMapper;
import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderOrchestratorOutboxEventEntity;
import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderWorkflowEntity;
import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderWorkflowReservationEntity;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderOrchestratorOutboxEventRepository;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderWorkflowRepository;
import com.iwos.orderorchestrator.infrastructure.observability.OrderWorkflowMetrics;
import com.iwos.orderorchestrator.infrastructure.source.entity.SourceOrderIntentEntity;
import com.iwos.orderorchestrator.infrastructure.source.entity.SourceOrderIntentItemEntity;
import com.iwos.orderorchestrator.infrastructure.source.entity.SourceOutboxEventEntity;
import com.iwos.orderorchestrator.infrastructure.source.repository.SourceOrderIntentRepository;
import com.iwos.orderorchestrator.infrastructure.source.repository.SourceOutboxEventRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderWorkflowProcessingService {

    private static final String SOURCE_EVENT_TYPE = "order-intake.accepted.v1";
    private static final String OUTBOX_STATUS_PENDING = "PENDING";

    private final SourceOutboxEventRepository sourceOutboxEventRepository;
    private final SourceOrderIntentRepository sourceOrderIntentRepository;
    private final OrderWorkflowRepository orderWorkflowRepository;
    private final OrderOrchestratorOutboxEventRepository outboxEventRepository;
    private final OrderWorkflowResponseMapper responseMapper;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderOrchestratorServiceProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final OrderWorkflowMetrics orderWorkflowMetrics;

    public OrderWorkflowProcessingService(
            SourceOutboxEventRepository sourceOutboxEventRepository,
            SourceOrderIntentRepository sourceOrderIntentRepository,
            OrderWorkflowRepository orderWorkflowRepository,
            OrderOrchestratorOutboxEventRepository outboxEventRepository,
            OrderWorkflowResponseMapper responseMapper,
            InventoryServiceClient inventoryServiceClient,
            OrderOrchestratorServiceProperties properties,
            TransactionTemplate transactionTemplate,
            ObjectMapper objectMapper,
            OrderWorkflowMetrics orderWorkflowMetrics
    ) {
        this.sourceOutboxEventRepository = sourceOutboxEventRepository;
        this.sourceOrderIntentRepository = sourceOrderIntentRepository;
        this.orderWorkflowRepository = orderWorkflowRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.responseMapper = responseMapper;
        this.inventoryServiceClient = inventoryServiceClient;
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
        this.orderWorkflowMetrics = orderWorkflowMetrics;
    }

    public ProcessPendingWorkflowsResponse processPending(int requestedLimit) {
        int limit = Math.min(requestedLimit, properties.getPendingFetchLimit());
        List<OrderWorkflowResponse> results = sourceOutboxEventRepository.findPendingAcceptedOrderIntentEvents(limit)
                .stream()
                .map(this::processAcceptedEvent)
                .toList();

        return new ProcessPendingWorkflowsResponse(results.size(), results);
    }

    public OrderWorkflowResponse processOrderIntent(UUID orderIntentId) {
        Optional<OrderWorkflowResponse> existingWorkflow = orderWorkflowRepository.findByOrderIntentId(orderIntentId)
                .map(responseMapper::toResponse);
        if (existingWorkflow.isPresent()) {
            orderWorkflowMetrics.recordWorkflowProcessed("existing", orderWorkflowMetrics.startWorkflowTimer());
            return existingWorkflow.get();
        }

        SourceOutboxEventEntity sourceEvent = sourceOutboxEventRepository
                .findFirstByAggregateIdAndEventTypeOrderByCreatedAtAsc(orderIntentId, SOURCE_EVENT_TYPE)
                .orElseThrow(() -> new OrderIntentSourceEventNotFoundException(orderIntentId));

        return processAcceptedEvent(sourceEvent);
    }

    private OrderWorkflowResponse processAcceptedEvent(SourceOutboxEventEntity sourceEvent) {
        var workflowTimer = orderWorkflowMetrics.startWorkflowTimer();
        try {
            Optional<OrderWorkflowResponse> existingWorkflow = orderWorkflowRepository
                    .findBySourceOutboxEventId(sourceEvent.getOutboxEventId())
                    .map(responseMapper::toResponse);
            if (existingWorkflow.isPresent()) {
                orderWorkflowMetrics.recordWorkflowProcessed("existing", workflowTimer);
                return existingWorkflow.get();
            }

            SourceOrderIntentEntity orderIntent = sourceOrderIntentRepository
                    .findWithItemsByOrderIntentId(sourceEvent.getAggregateId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Source order intent not found for aggregateId=%s".formatted(sourceEvent.getAggregateId())
                    ));

            OrderWorkflowEntity workflow;
            try {
                workflow = transactionTemplate.execute(status -> initializeWorkflow(sourceEvent, orderIntent));
            } catch (DataIntegrityViolationException exception) {
                OrderWorkflowResponse response = orderWorkflowRepository.findBySourceOutboxEventId(sourceEvent.getOutboxEventId())
                        .map(responseMapper::toResponse)
                        .orElseThrow(() -> exception);
                orderWorkflowMetrics.recordWorkflowProcessed("existing", workflowTimer);
                return response;
            }

            List<WorkflowReservationDraft> reservationDrafts = new ArrayList<>();
            try {
                for (SourceOrderIntentItemEntity item : orderIntent.getItems().stream()
                        .sorted((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()))
                        .toList()) {
                    InventoryReservationClientResponse reservation = inventoryServiceClient.createReservation(
                            reserveIdempotencyKey(orderIntent.getOrderIntentId(), item.getOrderIntentItemId()),
                            new InventoryCreateReservationRequest(
                                    orderIntent.getOrderIntentId().toString(),
                                    workflow.getFulfillmentNodeId(),
                                    item.getSku(),
                                    item.getQuantity()
                            )
                    );
                    reservationDrafts.add(new WorkflowReservationDraft(
                            item.getOrderIntentItemId(),
                            reservation.reservationId(),
                            reservation.nodeId(),
                            reservation.sku(),
                            reservation.quantity(),
                            OrderWorkflowReservationStatus.RESERVED
                    ));
                }

                OrderWorkflowResponse response =
                        transactionTemplate.execute(status -> finalizeSuccess(workflow.getWorkflowId(), reservationDrafts));
                orderWorkflowMetrics.recordWorkflowProcessed("inventory_reserved", workflowTimer);
                return response;
            } catch (RuntimeException exception) {
                List<WorkflowReservationDraft> compensatedReservations = compensateReservations(
                        orderIntent.getOrderIntentId(),
                        reservationDrafts
                );
                orderWorkflowMetrics.recordReservationCompensation(compensatedReservations.size());
                OrderWorkflowResponse response = transactionTemplate.execute(status ->
                        finalizeFailure(workflow.getWorkflowId(), compensatedReservations, exception.getMessage()));
                orderWorkflowMetrics.recordWorkflowProcessed("inventory_reservation_failed", workflowTimer);
                return response;
            }
        } catch (RuntimeException exception) {
            orderWorkflowMetrics.recordWorkflowProcessed("failed", workflowTimer);
            throw exception;
        }
    }

    private OrderWorkflowEntity initializeWorkflow(
            SourceOutboxEventEntity sourceEvent,
            SourceOrderIntentEntity orderIntent
    ) {
        OrderWorkflowEntity workflow = new OrderWorkflowEntity();
        workflow.setWorkflowId(UUID.randomUUID());
        workflow.setOrderIntentId(orderIntent.getOrderIntentId());
        workflow.setSourceOutboxEventId(sourceEvent.getOutboxEventId());
        workflow.setCustomerId(orderIntent.getCustomerId());
        workflow.setFulfillmentNodeId(properties.getDefaultFulfillmentNodeId());
        workflow.setStatus(OrderWorkflowStatus.INVENTORY_RESERVATION_IN_PROGRESS);
        workflow.setAcceptedAt(orderIntent.getAcceptedAt());
        return orderWorkflowRepository.saveAndFlush(workflow);
    }

    private OrderWorkflowResponse finalizeSuccess(UUID workflowId, List<WorkflowReservationDraft> reservationDrafts) {
        OrderWorkflowEntity workflow = orderWorkflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow disappeared during success finalization"));

        workflow.setStatus(OrderWorkflowStatus.INVENTORY_RESERVED);
        workflow.setFailureReason(null);
        workflow.setCompletedAt(Instant.now());

        reservationDrafts.forEach(draft -> workflow.addReservation(toReservationEntity(draft)));

        outboxEventRepository.save(outboxEvent(
                workflow.getWorkflowId(),
                "order-orchestrator.inventory-reserved.v1",
                Map.of(
                        "workflowId", workflow.getWorkflowId(),
                        "orderIntentId", workflow.getOrderIntentId(),
                        "reservationIds", reservationDrafts.stream().map(WorkflowReservationDraft::inventoryReservationId).toList(),
                        "occurredAt", workflow.getCompletedAt()
                )
        ));

        orderWorkflowRepository.saveAndFlush(workflow);
        return orderWorkflowRepository.findDetailedByWorkflowId(workflow.getWorkflowId())
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("Workflow disappeared after success finalization"));
    }

    private OrderWorkflowResponse finalizeFailure(
            UUID workflowId,
            List<WorkflowReservationDraft> reservationDrafts,
            String failureReason
    ) {
        OrderWorkflowEntity workflow = orderWorkflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalStateException("Workflow disappeared during failure finalization"));

        workflow.setStatus(OrderWorkflowStatus.INVENTORY_RESERVATION_FAILED);
        workflow.setFailureReason(trimFailureReason(failureReason));
        workflow.setCompletedAt(Instant.now());

        reservationDrafts.forEach(draft -> workflow.addReservation(toReservationEntity(draft)));

        outboxEventRepository.save(outboxEvent(
                workflow.getWorkflowId(),
                "order-orchestrator.inventory-reservation-failed.v1",
                Map.of(
                        "workflowId", workflow.getWorkflowId(),
                        "orderIntentId", workflow.getOrderIntentId(),
                        "reason", trimFailureReason(failureReason),
                        "occurredAt", workflow.getCompletedAt()
                )
        ));

        orderWorkflowRepository.saveAndFlush(workflow);
        return orderWorkflowRepository.findDetailedByWorkflowId(workflow.getWorkflowId())
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("Workflow disappeared after failure finalization"));
    }

    private List<WorkflowReservationDraft> compensateReservations(
            UUID orderIntentId,
            List<WorkflowReservationDraft> reservationDrafts
    ) {
        List<WorkflowReservationDraft> compensated = new ArrayList<>();
        for (WorkflowReservationDraft reservationDraft : reservationDrafts) {
            inventoryServiceClient.releaseReservation(
                    reservationDraft.inventoryReservationId(),
                    releaseIdempotencyKey(orderIntentId, reservationDraft.orderIntentItemId()),
                    new InventoryReservationActionRequest(
                            "ORCHESTRATION_COMPENSATION",
                            "ORDER_WORKFLOW",
                            orderIntentId.toString()
                    )
            );
            compensated.add(reservationDraft.released());
        }
        return compensated;
    }

    private OrderWorkflowReservationEntity toReservationEntity(WorkflowReservationDraft draft) {
        OrderWorkflowReservationEntity reservation = new OrderWorkflowReservationEntity();
        reservation.setWorkflowReservationId(UUID.randomUUID());
        reservation.setOrderIntentItemId(draft.orderIntentItemId());
        reservation.setInventoryReservationId(draft.inventoryReservationId());
        reservation.setNodeId(draft.nodeId());
        reservation.setSku(draft.sku());
        reservation.setQuantity(draft.quantity());
        reservation.setStatus(draft.status());
        return reservation;
    }

    private OrderOrchestratorOutboxEventEntity outboxEvent(UUID workflowId, String eventType, Map<String, Object> payload) {
        OrderOrchestratorOutboxEventEntity event = new OrderOrchestratorOutboxEventEntity();
        event.setOutboxEventId(UUID.randomUUID());
        event.setAggregateType("ORDER_WORKFLOW");
        event.setAggregateId(workflowId);
        event.setEventType(eventType);
        event.setStatus(OUTBOX_STATUS_PENDING);
        event.setPayload(writeJson(payload));
        return event;
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize order workflow outbox payload", exception);
        }
    }

    private String reserveIdempotencyKey(UUID orderIntentId, UUID orderIntentItemId) {
        return "order-orchestrator:reserve:" + orderIntentId + ":" + orderIntentItemId;
    }

    private String releaseIdempotencyKey(UUID orderIntentId, UUID orderIntentItemId) {
        return "order-orchestrator:release:" + orderIntentId + ":" + orderIntentItemId;
    }

    private String trimFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Unknown inventory reservation failure";
        }
        if (failureReason.length() <= 512) {
            return failureReason;
        }
        return failureReason.substring(0, 512);
    }
}
