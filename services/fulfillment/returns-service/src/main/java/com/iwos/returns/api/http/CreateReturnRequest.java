package com.iwos.returns.api.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateReturnRequest(
        @NotNull UUID orderIntentId,
        @NotNull UUID fulfillmentOrderId,
        UUID shipmentId,
        @NotBlank String customerId,
        @NotBlank String nodeId,
        @NotBlank String reasonCode,
        String reasonDetail,
        @NotEmpty List<@Valid ReturnLineItemRequest> items
) {
}
