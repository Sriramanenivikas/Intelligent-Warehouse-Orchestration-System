package com.iwos.warehouseorchestrator.application;

import com.iwos.warehouseorchestrator.api.http.FulfillmentOrderResponse;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderNotFoundException;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import com.iwos.warehouseorchestrator.infrastructure.persistence.FulfillmentOrderResponseMapper;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentOrderEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.FulfillmentOrderRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FulfillmentOrderQueryService {

    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final FulfillmentOrderResponseMapper mapper;

    public FulfillmentOrderQueryService(
            FulfillmentOrderRepository fulfillmentOrderRepository,
            FulfillmentOrderResponseMapper mapper
    ) {
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public FulfillmentOrderResponse getById(UUID fulfillmentOrderId) {
        FulfillmentOrderEntity entity = fulfillmentOrderRepository
                .findDetailedByFulfillmentOrderId(fulfillmentOrderId)
                .orElseThrow(() -> new FulfillmentOrderNotFoundException(fulfillmentOrderId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public FulfillmentOrderResponse getByWorkflowId(UUID workflowId) {
        FulfillmentOrderEntity entity = fulfillmentOrderRepository
                .findByWorkflowId(workflowId)
                .orElseThrow(() -> new FulfillmentOrderNotFoundException(workflowId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public FulfillmentOrderResponse getByOrderIntentId(UUID orderIntentId) {
        FulfillmentOrderEntity entity = fulfillmentOrderRepository
                .findByOrderIntentId(orderIntentId)
                .orElseThrow(() -> new FulfillmentOrderNotFoundException(orderIntentId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<FulfillmentOrderResponse> list(String status, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<FulfillmentOrderEntity> entities;
        if (status != null && !status.isBlank()) {
            FulfillmentOrderStatus parsedStatus = FulfillmentOrderStatus.valueOf(status);
            entities = fulfillmentOrderRepository.findAllByStatusOrderByCreatedAtDesc(parsedStatus, page);
        } else {
            entities = fulfillmentOrderRepository.findAllByOrderByCreatedAtDesc(page);
        }
        return entities.stream().map(mapper::toResponse).toList();
    }
}
