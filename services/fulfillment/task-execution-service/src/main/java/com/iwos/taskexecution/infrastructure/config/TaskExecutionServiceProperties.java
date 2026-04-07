package com.iwos.taskexecution.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task-execution-service")
public record TaskExecutionServiceProperties(
        String serviceName,
        Kafka kafka,
        Warehouse warehouse,
        Inventory inventory
) {
    public record Kafka(
            String consumerTopic,
            String producerTopic,
            String groupId
    ) {}

    public record Warehouse(
            String baseUrl
    ) {}

    public record Inventory(
            String baseUrl
    ) {}
}
