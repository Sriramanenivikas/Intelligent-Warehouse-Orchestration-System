package com.iwos.promiseallocation.api.http;

import com.iwos.promiseallocation.domain.promise.PromiseAllocationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromiseEvaluationResponse(
        UUID evaluationId,
        PromiseAllocationStatus status,
        String fulfillmentNodeId,
        String reason,
        Instant promisedBy,
        Instant evaluatedAt,
        String customerId,
        List<PromiseItemDecisionResponse> items
) {
}
