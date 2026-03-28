package com.iwos.orderorchestrator.api.http;

import com.iwos.orderorchestrator.application.OrderWorkflowProcessingService;
import com.iwos.orderorchestrator.application.OrderWorkflowQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/order-workflows")
public class OrderWorkflowController {

    private final OrderWorkflowProcessingService orderWorkflowProcessingService;
    private final OrderWorkflowQueryService orderWorkflowQueryService;

    public OrderWorkflowController(
            OrderWorkflowProcessingService orderWorkflowProcessingService,
            OrderWorkflowQueryService orderWorkflowQueryService
    ) {
        this.orderWorkflowProcessingService = orderWorkflowProcessingService;
        this.orderWorkflowQueryService = orderWorkflowQueryService;
    }

    @PostMapping("/process-pending")
    public ProcessPendingWorkflowsResponse processPending(
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return orderWorkflowProcessingService.processPending(limit);
    }

    @PostMapping("/{orderIntentId}/process")
    public OrderWorkflowResponse processOrderIntent(@PathVariable("orderIntentId") UUID orderIntentId) {
        return orderWorkflowProcessingService.processOrderIntent(orderIntentId);
    }

    @GetMapping("/{orderIntentId}")
    public OrderWorkflowResponse getOrderWorkflow(@PathVariable("orderIntentId") UUID orderIntentId) {
        return orderWorkflowQueryService.getOrderWorkflow(orderIntentId);
    }
}
