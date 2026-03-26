package com.iwos.common.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {}

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateOrderId() {
        return "ORD-" + System.currentTimeMillis() + "-" + generateId().substring(0, 6).toUpperCase();
    }

    public static String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis() + "-" + generateId().substring(0, 6).toUpperCase();
    }

    public static String generateTrackingId() {
        return "TRK-" + System.currentTimeMillis() + "-" + generateId().substring(0, 8).toUpperCase();
    }
}
