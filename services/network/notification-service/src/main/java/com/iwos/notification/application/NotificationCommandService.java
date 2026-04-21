package com.iwos.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.notification.domain.notification.NotificationAudience;
import com.iwos.notification.domain.notification.NotificationChannel;
import com.iwos.notification.domain.notification.NotificationStatus;
import com.iwos.notification.infrastructure.observability.NotificationMetrics;
import com.iwos.notification.infrastructure.persistence.entity.NotificationEntity;
import com.iwos.notification.infrastructure.persistence.entity.NotificationOutboxEventEntity;
import com.iwos.notification.infrastructure.persistence.repository.NotificationOutboxEventRepository;
import com.iwos.notification.infrastructure.persistence.repository.NotificationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxEventRepository outboxRepository;
    private final NotificationMetrics metrics;
    private final ObjectMapper objectMapper;

    public NotificationCommandService(
            NotificationRepository notificationRepository,
            NotificationOutboxEventRepository outboxRepository,
            NotificationMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void consumeScanMilestoneEvent(Map<String, Object> event) {
        String eventType = required(event, "eventType");
        if (!"scan.milestone-recorded.v1".equals(eventType)) {
            metrics.incrementInboundEventsSkipped();
            return;
        }

        List<NotificationEntity> notifications = buildNotifications(event);
        if (notifications.isEmpty()) {
            metrics.incrementInboundEventsSkipped();
            return;
        }

        notificationRepository.saveAll(notifications);
        notifications.forEach(notification -> createOutboxEvent(notification, event));
        metrics.incrementNotificationsGenerated(notifications.size());
        metrics.incrementInboundEventsProcessed();
    }

    private List<NotificationEntity> buildNotifications(Map<String, Object> event) {
        List<NotificationEntity> notifications = new ArrayList<>();
        String scanType = required(event, "scanType");

        NotificationEntity customerNotification = customerNotificationFor(event, scanType);
        if (customerNotification != null) {
            notifications.add(customerNotification);
        }

        if ("EXCEPTION".equals(scanType)) {
            notifications.add(buildNotification(
                    event,
                    NotificationAudience.OPS,
                    NotificationChannel.WEBHOOK,
                    "ops_exception_alert",
                    "Delivery exception requires attention",
                    "Shipment %s has raised an exception milestone.".formatted(required(event, "awbNumber"))
            ));
        }

        return notifications;
    }

    private NotificationEntity customerNotificationFor(Map<String, Object> event, String scanType) {
        return switch (scanType) {
            case "MANIFESTED" -> buildNotification(
                    event,
                    NotificationAudience.CUSTOMER,
                    NotificationChannel.PUSH,
                    "shipment_manifested",
                    "Shipment confirmed",
                    "Your order has been packed and handed over for movement."
            );
            case "OUT_FOR_DELIVERY" -> buildNotification(
                    event,
                    NotificationAudience.CUSTOMER,
                    NotificationChannel.PUSH,
                    "out_for_delivery",
                    "Out for delivery",
                    "Your order is out for delivery today."
            );
            case "DELIVERED" -> buildNotification(
                    event,
                    NotificationAudience.CUSTOMER,
                    NotificationChannel.PUSH,
                    "delivered",
                    "Delivered",
                    "Your order has been delivered successfully."
            );
            case "EXCEPTION" -> buildNotification(
                    event,
                    NotificationAudience.CUSTOMER,
                    NotificationChannel.PUSH,
                    "delivery_exception",
                    "Delivery update",
                    "We hit an exception while processing your delivery and are checking it."
            );
            default -> null;
        };
    }

    private NotificationEntity buildNotification(
            Map<String, Object> event,
            NotificationAudience audience,
            NotificationChannel channel,
            String templateCode,
            String title,
            String message
    ) {
        Instant now = Instant.now();
        NotificationEntity notification = new NotificationEntity();
        notification.setNotificationId(UUID.randomUUID());
        notification.setExternalRefId(UUID.randomUUID());
        notification.setShipmentId(UUID.fromString(required(event, "shipmentId")));
        notification.setOrderIntentId(UUID.fromString(required(event, "orderIntentId")));
        notification.setTrackedShipmentId(optionalUuid(event, "trackedShipmentId"));
        notification.setAwbNumber(required(event, "awbNumber"));
        notification.setAudience(audience);
        notification.setChannel(channel);
        notification.setTemplateCode(templateCode);
        notification.setEventType(required(event, "eventType"));
        notification.setScanType(required(event, "scanType"));
        notification.setStatus(NotificationStatus.GENERATED);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMetadataJson(metadataJson(event));
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        return notification;
    }

    private void createOutboxEvent(NotificationEntity notification, Map<String, Object> sourceEvent) {
        try {
            Instant now = Instant.now();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventType", "notification.created.v1");
            payload.put("notificationId", notification.getNotificationId().toString());
            payload.put("shipmentId", notification.getShipmentId().toString());
            payload.put("orderIntentId", notification.getOrderIntentId().toString());
            payload.put("awbNumber", notification.getAwbNumber());
            payload.put("audience", notification.getAudience().name());
            payload.put("channel", notification.getChannel().name());
            payload.put("templateCode", notification.getTemplateCode());
            payload.put("title", notification.getTitle());
            payload.put("message", notification.getMessage());
            payload.put("scanType", notification.getScanType());
            payload.put("sourceEventType", sourceEvent.get("eventType"));
            payload.put("occurredAt", sourceEvent.get("occurredAt"));

            NotificationOutboxEventEntity outbox = new NotificationOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("NOTIFICATION");
            outbox.setAggregateId(notification.getNotificationId());
            outbox.setEventType("notification.created.v1");
            outbox.setStatus("PENDING");
            outbox.setAttempts(0);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setCreatedAt(now);
            outbox.setUpdatedAt(now);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create notification outbox event", e);
        }
    }

    private String metadataJson(Map<String, Object> sourceEvent) {
        try {
            return objectMapper.writeValueAsString(sourceEvent);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize notification metadata", e);
        }
    }

    private String required(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required event field: " + key);
        }
        return value.toString();
    }

    private UUID optionalUuid(Map<String, Object> event, String key) {
        Object value = event.get(key);
        return value == null || value.toString().isBlank() ? null : UUID.fromString(value.toString());
    }
}
