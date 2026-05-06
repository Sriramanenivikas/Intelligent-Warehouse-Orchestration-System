package com.iwos.identity.api.http;

import com.iwos.identity.domain.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        String message = exception instanceof MethodArgumentNotValidException ex
                ? ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(FieldError::getDefaultMessage)
                        .orElse("Request validation failed")
                : "Request validation failed";
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnhandled(
            Exception exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected internal error", request.getRequestURI());
    }

    private ApiErrorResponse error(HttpStatus status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status.value(), error, message, path);
    }
}
