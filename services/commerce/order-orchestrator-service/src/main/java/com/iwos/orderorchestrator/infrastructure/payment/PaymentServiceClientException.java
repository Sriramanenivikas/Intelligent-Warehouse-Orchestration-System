package com.iwos.orderorchestrator.infrastructure.payment;

import org.springframework.http.HttpStatus;

public class PaymentServiceClientException extends RuntimeException {

    private final HttpStatus httpStatus;

    public PaymentServiceClientException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public PaymentServiceClientException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
