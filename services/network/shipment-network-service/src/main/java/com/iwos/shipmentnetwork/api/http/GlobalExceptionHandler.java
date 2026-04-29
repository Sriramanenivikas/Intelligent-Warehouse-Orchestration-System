package com.iwos.shipmentnetwork.api.http;

import com.iwos.shipmentnetwork.domain.network.NetworkShipmentAlreadyExistsException;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentNotFoundException;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStateException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NetworkShipmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(NetworkShipmentNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NETWORK_SHIPMENT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(NetworkShipmentAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> conflict(NetworkShipmentAlreadyExistsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "NETWORK_SHIPMENT_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(NetworkShipmentStateException.class)
    public ResponseEntity<ApiErrorResponse> invalidState(NetworkShipmentStateException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "NETWORK_SHIPMENT_INVALID_STATE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                requestId(),
                Instant.now(),
                errors
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> argumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameter = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_ARGUMENT",
                "Invalid value for " + parameter + ": " + value,
                requestId(),
                Instant.now(),
                Map.of(parameter, "Invalid value")
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                requestId(),
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> noResourceFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> generic(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                code,
                message,
                requestId(),
                Instant.now(),
                Map.of()
        ));
    }

    private String requestId() {
        String requestId = MDC.get("request_id");
        return requestId != null ? requestId : "unknown";
    }
}
