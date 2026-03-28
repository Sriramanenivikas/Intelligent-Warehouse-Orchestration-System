package com.iwos.inventoryledger.application;

public record IdempotentCommandResult<T>(
        T response,
        boolean replayed
) {
}
