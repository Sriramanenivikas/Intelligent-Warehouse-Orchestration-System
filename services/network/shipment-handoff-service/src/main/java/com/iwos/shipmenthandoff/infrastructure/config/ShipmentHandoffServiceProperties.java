package com.iwos.shipmenthandoff.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shipment-handoff-service")
public record ShipmentHandoffServiceProperties(
        String serviceName,
        Kafka kafka,
        Warehouse warehouse,
        OrderIntake orderIntake
) {
    public record Kafka(
            String consumerTopic,
            String producerTopic,
            String groupId
    ) {}

    public record Warehouse(
            String baseUrl
    ) {}

    public record OrderIntake(
            String baseUrl
    ) {}
}
