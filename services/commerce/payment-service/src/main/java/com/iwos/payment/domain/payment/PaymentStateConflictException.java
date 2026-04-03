package com.iwos.payment.domain.payment;

import java.util.UUID;

public class PaymentStateConflictException extends RuntimeException {

    public PaymentStateConflictException(UUID paymentIntentId, String message) {
        super("Payment intent %s %s".formatted(paymentIntentId, message));
    }
}
