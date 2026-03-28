package com.iwos.orderorchestrator.infrastructure.inventory;

import org.springframework.http.HttpStatus;

public class InventoryServiceClientException extends RuntimeException {

    private final HttpStatus httpStatus;

    public InventoryServiceClientException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public InventoryServiceClientException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
