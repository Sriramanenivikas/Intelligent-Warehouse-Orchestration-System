package com.iwos.orderorchestrator.api.http;

import java.util.List;

public record ProcessPendingWorkflowsResponse(
        int processedCount,
        List<OrderWorkflowResponse> results
) {
}
