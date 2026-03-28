package com.iwos.inventoryledger.api.http;

import com.iwos.inventoryledger.application.IdempotentCommandResult;
import com.iwos.inventoryledger.application.InventoryQueryService;
import com.iwos.inventoryledger.application.InventoryStockCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class InventoryStockController {

    private final InventoryStockCommandService inventoryStockCommandService;
    private final InventoryQueryService inventoryQueryService;

    public InventoryStockController(
            InventoryStockCommandService inventoryStockCommandService,
            InventoryQueryService inventoryQueryService
    ) {
        this.inventoryStockCommandService = inventoryStockCommandService;
        this.inventoryQueryService = inventoryQueryService;
    }

    @PostMapping("/stock-adjustments")
    public ResponseEntity<StockAdjustmentResponse> createStockAdjustment(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        IdempotentCommandResult<StockAdjustmentResponse> result = inventoryStockCommandService.adjustStock(idempotencyKey, request);
        return ResponseEntity.accepted()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @GetMapping("/stock-items/{nodeId}/{sku}")
    public InventoryStockResponse getStockItem(
            @PathVariable("nodeId") String nodeId,
            @PathVariable("sku") String sku
    ) {
        return inventoryQueryService.getStock(nodeId, sku);
    }
}
