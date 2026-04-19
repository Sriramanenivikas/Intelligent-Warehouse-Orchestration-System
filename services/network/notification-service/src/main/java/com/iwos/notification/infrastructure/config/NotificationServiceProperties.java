package com.iwos.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification-service")
public record NotificationServiceProperties(
        String serviceName,
        KafkaProperties kafka,
        OutboxProperties outbox
) {

    public record KafkaProperties(
            String consumerTopic,
            String producerTopic,
            String groupId
    ) {
    }

    public record OutboxProperties(
            long pollIntervalMs
    ) {
    }
}
