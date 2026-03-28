package com.iwos.inventoryledger.domain.inventory;

public class InventoryStockNotFoundException extends RuntimeException {

    public InventoryStockNotFoundException(String nodeId, String sku) {
        super("Inventory stock item not found for nodeId=%s sku=%s".formatted(nodeId, sku));
    }
}
