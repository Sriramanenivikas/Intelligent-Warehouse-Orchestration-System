package com.iwos.notification.api.http;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID shipmentId,
        UUID orderIntentId,
        String awbNumber,
        String audience,
        String channel,
        String templateCode,
        String eventType,
        String scanType,
        String status,
        String title,
        String message,
        Instant createdAt,
        Instant deliveredAt
) {
}
