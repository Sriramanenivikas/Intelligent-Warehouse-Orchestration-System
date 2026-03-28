package com.iwos.orderorchestrator.api.http;

import com.iwos.orderorchestrator.domain.workflow.OrderIntentSourceEventNotFoundException;
import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(error("VALIDATION_FAILED", "Request validation failed", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations()
                .forEach(violation -> fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(error("VALIDATION_FAILED", "Request validation failed", fieldErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String fieldName = exception.getName() == null ? "request" : exception.getName();
        String expectedType = exception.getRequiredType() == null ? "valid value" : exception.getRequiredType().getSimpleName();
        return ResponseEntity.badRequest().body(error(
                "VALIDATION_FAILED",
                "Request validation failed",
                Map.of(fieldName, "Expected " + expectedType)
        ));
    }

    @ExceptionHandler(OrderWorkflowNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWorkflowNotFound(OrderWorkflowNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(
                "ORDER_WORKFLOW_NOT_FOUND",
                exception.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(OrderIntentSourceEventNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSourceEventNotFound(OrderIntentSourceEventNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(
                "ORDER_INTENT_SOURCE_EVENT_NOT_FOUND",
                exception.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleRouteNotFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(
                "RESOURCE_NOT_FOUND",
                "Requested resource was not found",
                Map.of()
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error(
                "METHOD_NOT_ALLOWED",
                exception.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled exception while processing request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                Map.of()
        ));
    }

    private ApiErrorResponse error(String code, String message, Map<String, String> fieldErrors) {
        return new ApiErrorResponse(code, message, MDC.get("request_id"), Instant.now(), fieldErrors);
    }
}
