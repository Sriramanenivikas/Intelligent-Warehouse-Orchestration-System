package com.iwos.shipmenthandoff.api.http;

import com.iwos.shipmenthandoff.domain.shipment.ShipmentAlreadyExistsException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentNotFoundException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStateException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ShipmentNotFoundException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "SHIPMENT_NOT_FOUND",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(ShipmentAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyExists(
            ShipmentAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "SHIPMENT_ALREADY_EXISTS",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(ShipmentStateException.class)
    public ResponseEntity<ApiErrorResponse> handleStateException(
            ShipmentStateException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "INVALID_SHIPMENT_STATE",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                requestId,
                Instant.now(),
                fieldErrors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        log.error("Unhandled exception while processing request", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                requestId,
                Instant.now(),
                Map.of()
        ));
    }
}
