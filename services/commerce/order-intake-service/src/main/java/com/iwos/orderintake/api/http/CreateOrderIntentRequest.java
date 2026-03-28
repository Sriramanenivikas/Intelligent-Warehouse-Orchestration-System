package com.iwos.orderintake.api.http;

import com.iwos.orderintake.domain.order.OrderChannel;
import com.iwos.orderintake.domain.order.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderIntentRequest(
        @NotBlank String customerId,
        @NotNull OrderChannel channel,
        @NotNull PaymentMode paymentMode,
        @NotBlank String currency,
        @NotNull @Valid AddressPayload deliveryAddress,
        @NotEmpty List<@Valid OrderItemPayload> items
) {
}
