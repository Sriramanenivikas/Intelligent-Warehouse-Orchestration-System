package com.iwos.promiseallocation.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.promiseallocation.api.http.PromiseEvaluationResponse;
import com.iwos.promiseallocation.api.http.PromiseItemDecisionResponse;
import com.iwos.promiseallocation.api.http.PromiseItemRequest;
import com.iwos.promiseallocation.api.http.ResolvePromiseRequest;
import com.iwos.promiseallocation.domain.promise.PromiseAllocationStatus;
import com.iwos.promiseallocation.infrastructure.config.PromiseAllocationServiceProperties;
import com.iwos.promiseallocation.infrastructure.inventory.InventoryLookupClient;
import com.iwos.promiseallocation.infrastructure.inventory.InventoryStockClientResponse;
import com.iwos.promiseallocation.infrastructure.observability.PromiseAllocationMetrics;
import com.iwos.promiseallocation.infrastructure.persistence.PromiseEvaluationResponseMapper;
import com.iwos.promiseallocation.infrastructure.persistence.entity.NodeProfileEntity;
import com.iwos.promiseallocation.infrastructure.persistence.entity.PromiseEvaluationEntity;
import com.iwos.promiseallocation.infrastructure.persistence.repository.NodeProfileRepository;
import com.iwos.promiseallocation.infrastructure.persistence.repository.PromiseEvaluationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromiseAllocationService {

    private final NodeProfileRepository nodeProfileRepository;
    private final PromiseEvaluationRepository promiseEvaluationRepository;
    private final PromiseEvaluationResponseMapper responseMapper;
    private final InventoryLookupClient inventoryLookupClient;
    private final PromiseAllocationServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final PromiseAllocationMetrics metrics;

    public PromiseAllocationService(
            NodeProfileRepository nodeProfileRepository,
            PromiseEvaluationRepository promiseEvaluationRepository,
            PromiseEvaluationResponseMapper responseMapper,
            InventoryLookupClient inventoryLookupClient,
            PromiseAllocationServiceProperties properties,
            ObjectMapper objectMapper,
            PromiseAllocationMetrics metrics
    ) {
        this.nodeProfileRepository = nodeProfileRepository;
        this.promiseEvaluationRepository = promiseEvaluationRepository;
        this.responseMapper = responseMapper;
        this.inventoryLookupClient = inventoryLookupClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Transactional
    public PromiseEvaluationResponse resolve(ResolvePromiseRequest request) {
        var timer = metrics.startResolveTimer();
        try {
            List<NodeProfileEntity> nodes = nodeProfileRepository.findByActiveTrueOrderByPriorityAsc();
            if (nodes.isEmpty()) {
                PromiseEvaluationResponse response = persistEvaluation(
                        request,
                        PromiseAllocationStatus.UNFULFILLABLE,
                        null,
                        "No active fulfillment nodes are configured",
                        null,
                        request.items().stream()
                                .map(item -> new PromiseItemDecisionResponse(item.sku(), item.quantity(), 0, false))
                                .toList()
                );
                metrics.recordResolve("unfulfillable", timer);
                return response;
            }

            List<PromiseItemDecisionResponse> lastDecisionSet = List.of();
            for (NodeProfileEntity node : nodes) {
                List<PromiseItemDecisionResponse> decisions = evaluateNode(node.getNodeId(), request.items());
                boolean allFulfillable = decisions.stream().allMatch(PromiseItemDecisionResponse::fulfillable);
                if (allFulfillable) {
                    PromiseEvaluationResponse response = persistEvaluation(
                            request,
                            PromiseAllocationStatus.ALLOCATED,
                            node.getNodeId(),
                            "Inventory available on selected node",
                            Instant.now().plus(properties.getPromisedDuration()),
                            decisions
                    );
                    metrics.recordResolve("allocated", timer);
                    return response;
                }
                lastDecisionSet = decisions;
            }

            PromiseEvaluationResponse response = persistEvaluation(
                    request,
                    PromiseAllocationStatus.UNFULFILLABLE,
                    null,
                    "No active node has enough inventory for the full order",
                    null,
                    lastDecisionSet
            );
            metrics.recordResolve("unfulfillable", timer);
            return response;
        } catch (RuntimeException exception) {
            metrics.recordResolve("failed", timer);
            throw exception;
        }
    }

    private List<PromiseItemDecisionResponse> evaluateNode(String nodeId, List<PromiseItemRequest> items) {
        List<PromiseItemDecisionResponse> decisions = new ArrayList<>();
        for (PromiseItemRequest item : items) {
            Optional<InventoryStockClientResponse> stock = inventoryLookupClient.getStock(nodeId, item.sku());
            int availableQuantity = stock.map(InventoryStockClientResponse::availableQuantity).orElse(0);
            decisions.add(new PromiseItemDecisionResponse(
                    item.sku(),
                    item.quantity(),
                    availableQuantity,
                    availableQuantity >= item.quantity()
            ));
        }
        return decisions;
    }

    private PromiseEvaluationResponse persistEvaluation(
            ResolvePromiseRequest request,
            PromiseAllocationStatus status,
            String fulfillmentNodeId,
            String reason,
            Instant promisedBy,
            List<PromiseItemDecisionResponse> decisions
    ) {
        PromiseEvaluationEntity entity = new PromiseEvaluationEntity();
        entity.setEvaluationId(UUID.randomUUID());
        entity.setCustomerId(request.customerId());
        entity.setStatus(status);
        entity.setFulfillmentNodeId(fulfillmentNodeId);
        entity.setReason(reason);
        entity.setPromisedBy(promisedBy);
        entity.setEvaluatedAt(Instant.now());
        entity.setDeliveryAddressJson(writeJson(request.deliveryAddress()));
        entity.setRequestedItemsJson(writeJson(request.items()));
        entity.setItemDecisionsJson(writeJson(decisions));

        PromiseEvaluationEntity persisted = promiseEvaluationRepository.save(entity);
        return responseMapper.toResponse(persisted);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize promise allocation payload", exception);
        }
    }
}
