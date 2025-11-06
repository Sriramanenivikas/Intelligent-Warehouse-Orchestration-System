package com.iwos.exception;

/**
 * Custom Token Exception
 * Thrown when token operations fail
 */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
