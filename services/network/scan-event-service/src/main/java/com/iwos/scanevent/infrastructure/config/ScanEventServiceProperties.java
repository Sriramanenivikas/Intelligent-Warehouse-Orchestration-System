package com.iwos.scanevent.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scan-event-service")
public record ScanEventServiceProperties(
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
