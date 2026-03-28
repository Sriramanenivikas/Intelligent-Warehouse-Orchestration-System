package com.iwos.orderintake.api.http;

import com.iwos.orderintake.domain.order.OrderChannel;
import com.iwos.orderintake.domain.order.OrderIntentStatus;
import com.iwos.orderintake.domain.order.PaymentMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderIntentResponse(
        UUID orderIntentId,
        String customerId,
        OrderChannel channel,
        PaymentMode paymentMode,
        String currency,
        OrderIntentStatus status,
        Instant acceptedAt,
        AddressPayload deliveryAddress,
        List<OrderItemPayload> items
) {
}
