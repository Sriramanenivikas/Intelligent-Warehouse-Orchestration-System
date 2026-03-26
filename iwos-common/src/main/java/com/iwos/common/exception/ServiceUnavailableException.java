package com.iwos.common.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {

    public ServiceUnavailableException(String serviceName) {
        super(
            String.format("Service '%s' is currently unavailable", serviceName),
            HttpStatus.SERVICE_UNAVAILABLE,
            "SERVICE_UNAVAILABLE"
        );
    }
}
