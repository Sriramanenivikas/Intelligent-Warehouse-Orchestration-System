package com.iwos.notification.infrastructure.persistence;

import com.iwos.notification.api.http.NotificationResponse;
import com.iwos.notification.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationResponseMapper {

    public NotificationResponse toResponse(NotificationEntity notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getShipmentId(),
                notification.getOrderIntentId(),
                notification.getAwbNumber(),
                notification.getAudience().name(),
                notification.getChannel().name(),
                notification.getTemplateCode(),
                notification.getEventType(),
                notification.getScanType(),
                notification.getStatus().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getDeliveredAt()
        );
    }
}
