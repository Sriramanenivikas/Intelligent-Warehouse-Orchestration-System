package com.iwos.orderintake.application;

import com.iwos.orderintake.api.http.OrderIntentResponse;

public record OrderIntentAcceptedResult(
        OrderIntentResponse response,
        boolean replayed
) {
}
