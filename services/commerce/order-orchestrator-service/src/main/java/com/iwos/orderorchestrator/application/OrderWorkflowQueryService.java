package com.iwos.orderorchestrator.application;

import com.iwos.orderorchestrator.api.http.OrderWorkflowResponse;
import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowNotFoundException;
import com.iwos.orderorchestrator.infrastructure.persistence.OrderWorkflowResponseMapper;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderWorkflowRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderWorkflowQueryService {

    private final OrderWorkflowRepository orderWorkflowRepository;
    private final OrderWorkflowResponseMapper responseMapper;

    public OrderWorkflowQueryService(
            OrderWorkflowRepository orderWorkflowRepository,
            OrderWorkflowResponseMapper responseMapper
    ) {
        this.orderWorkflowRepository = orderWorkflowRepository;
        this.responseMapper = responseMapper;
    }

    public OrderWorkflowResponse getOrderWorkflow(UUID orderIntentId) {
        return orderWorkflowRepository.findByOrderIntentId(orderIntentId)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new OrderWorkflowNotFoundException(orderIntentId));
    }
}
