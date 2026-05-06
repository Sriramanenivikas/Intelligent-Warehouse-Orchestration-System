package com.iwos.controltower.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "control-tower-service")
public record ControlTowerServiceProperties(
        String serviceName,
        SchedulerProperties scheduler,
        SnapshotProperties snapshot
) {

    public record SchedulerProperties(
            String refreshCron
    ) {
    }

    public record SnapshotProperties(
            int topLimit
    ) {
    }
}
