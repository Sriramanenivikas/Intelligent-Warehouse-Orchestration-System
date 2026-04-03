package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWorkflowErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId
) {
}
