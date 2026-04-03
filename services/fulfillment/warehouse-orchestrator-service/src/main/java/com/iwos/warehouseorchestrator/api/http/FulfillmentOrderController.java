package com.iwos.warehouseorchestrator.api.http;

import com.iwos.warehouseorchestrator.application.FulfillmentOrderQueryService;
import com.iwos.warehouseorchestrator.application.WarehouseFulfillmentProcessingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/fulfillment-orders")
public class FulfillmentOrderController {

    private final FulfillmentOrderQueryService queryService;
    private final WarehouseFulfillmentProcessingService processingService;

    public FulfillmentOrderController(
            FulfillmentOrderQueryService queryService,
            WarehouseFulfillmentProcessingService processingService
    ) {
        this.queryService = queryService;
        this.processingService = processingService;
    }

    @PostMapping("/{orderIntentId}/process")
    public ResponseEntity<Map<String, Object>> processOrder(@PathVariable("orderIntentId") UUID orderIntentId) {
        UUID fulfillmentOrderId = processingService.processFulfillment(orderIntentId, "manual", "manual-" + orderIntentId, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "fulfillmentOrderId", fulfillmentOrderId,
                "orderIntentId", orderIntentId,
                "status", "TASKS_CREATED"
        ));
    }

    @GetMapping("/{fulfillmentOrderId}")
    public FulfillmentOrderResponse getById(@PathVariable("fulfillmentOrderId") UUID fulfillmentOrderId) {
        return queryService.getById(fulfillmentOrderId);
    }

    @GetMapping("/by-order-intent/{orderIntentId}")
    public FulfillmentOrderResponse getByOrderIntentId(@PathVariable("orderIntentId") UUID orderIntentId) {
        return queryService.getByOrderIntentId(orderIntentId);
    }

    @GetMapping("/by-workflow/{workflowId}")
    public FulfillmentOrderResponse getByWorkflowId(@PathVariable("workflowId") UUID workflowId) {
        return queryService.getByWorkflowId(workflowId);
    }

    @GetMapping
    public List<FulfillmentOrderResponse> list(
            @RequestParam(name = "status", required = false) @Nullable String status,
            @RequestParam(name = "limit", defaultValue = "25") @Min(1) @Max(100) int limit
    ) {
        return queryService.list(status, limit);
    }
}
