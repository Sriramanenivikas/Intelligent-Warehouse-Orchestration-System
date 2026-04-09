package com.iwos.promiseallocation.infrastructure.inventory;

public class InventoryLookupException extends RuntimeException {

    public InventoryLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryLookupException(String message) {
        super(message);
    }
}
