package com.iwos.shipmentnetwork.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "shipment-network-service")
public record ShipmentNetworkServiceProperties(
        @NotBlank String serviceName,
        KafkaProperties kafka,
        OutboxProperties outbox
) {
    public record KafkaProperties(
            @NotBlank String consumerTopic,
            @NotBlank String producerTopic,
            @NotBlank String groupId
    ) {
    }

    public record OutboxProperties(long pollIntervalMs) {
    }
}
