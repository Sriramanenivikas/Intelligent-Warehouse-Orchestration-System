package com.iwos.orderorchestrator.infrastructure.promise;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromiseAllocationClientResponse(
        UUID evaluationId,
        String status,
        String fulfillmentNodeId,
        String reason,
        Instant promisedBy,
        Instant evaluatedAt,
        String customerId,
        List<PromiseItemDecisionResponse> items
) {
}
