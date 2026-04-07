package com.iwos.taskexecution.api.http;

import com.iwos.taskexecution.domain.task.TaskAlreadyClaimedException;
import com.iwos.taskexecution.domain.task.TaskNotFoundException;
import com.iwos.taskexecution.domain.task.TaskStateException;
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

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskNotFound(
            TaskNotFoundException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "TASK_NOT_FOUND",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(TaskStateException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskStateException(
            TaskStateException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "INVALID_TASK_STATE",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
        ));
    }

    @ExceptionHandler(TaskAlreadyClaimedException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskAlreadyClaimed(
            TaskAlreadyClaimedException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "TASK_ALREADY_CLAIMED",
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
