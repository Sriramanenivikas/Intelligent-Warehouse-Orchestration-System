package com.iwos.inventoryledger.domain.inventory;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String nodeId, String sku, int requestedQuantity) {
        super("Insufficient stock for nodeId=%s sku=%s requestedQuantity=%d"
                .formatted(nodeId, sku, requestedQuantity));
    }
}
