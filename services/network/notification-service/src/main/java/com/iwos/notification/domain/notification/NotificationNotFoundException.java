package com.iwos.notification.domain.notification;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID notificationId) {
        super("Notification not found for notificationId=" + notificationId);
    }
}
