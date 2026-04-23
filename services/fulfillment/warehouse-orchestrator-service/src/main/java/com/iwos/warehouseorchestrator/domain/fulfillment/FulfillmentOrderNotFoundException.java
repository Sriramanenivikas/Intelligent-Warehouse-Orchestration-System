package com.iwos.warehouseorchestrator.domain.fulfillment;

import java.util.UUID;

public class FulfillmentOrderNotFoundException extends RuntimeException {

    public FulfillmentOrderNotFoundException(UUID id) {
        super("Fulfillment order not found for id=%s".formatted(id));
    }
}
