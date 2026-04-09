package com.iwos.payment.domain.payment;

import java.util.UUID;

public class PaymentIntentNotFoundException extends RuntimeException {

    public PaymentIntentNotFoundException(UUID paymentIntentId) {
        super("Payment intent not found: " + paymentIntentId);
    }
}
