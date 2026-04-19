package com.iwos.notification.api.http;

import com.iwos.notification.domain.notification.NotificationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(
            NotificationNotFoundException ex,
            HttpServletRequest request
    ) {
        String requestId = (String) request.getAttribute("requestId");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "NOTIFICATION_NOT_FOUND",
                ex.getMessage(),
                requestId,
                Instant.now(),
                Map.of()
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
