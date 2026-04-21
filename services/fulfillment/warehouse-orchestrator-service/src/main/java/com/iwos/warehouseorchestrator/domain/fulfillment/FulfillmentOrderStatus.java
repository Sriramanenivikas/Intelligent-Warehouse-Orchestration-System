package com.iwos.warehouseorchestrator.domain.fulfillment;

public enum FulfillmentOrderStatus {
    TASKS_CREATED,
    PICKING_IN_PROGRESS,
    PACKING_IN_PROGRESS,
    PACKED,
    FULFILLED,
    FAILED
}

