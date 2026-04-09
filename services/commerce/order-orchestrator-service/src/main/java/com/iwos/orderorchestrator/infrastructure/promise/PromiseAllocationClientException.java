package com.iwos.orderorchestrator.infrastructure.promise;

import org.springframework.http.HttpStatus;

public class PromiseAllocationClientException extends RuntimeException {

    private final HttpStatus httpStatus;

    public PromiseAllocationClientException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public PromiseAllocationClientException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
