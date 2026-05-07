package com.iwos.forecasting.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forecasting-planning-service")
public record ForecastingPlanningServiceProperties(
        String serviceName,
        SchedulerProperties scheduler,
        PipelineProperties pipeline,
        ModelProperties model
) {

    public record SchedulerProperties(
            String refreshCron
    ) {
    }

    public record PipelineProperties(
            String mode
    ) {
    }

    public record ModelProperties(
            String version,
            int leadTimeHours,
            int safetyStockHours,
            int lookbackHours
    ) {
    }
}
