package com.iwos.forecasting.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forecasting-planning-service")
public record ForecastingPlanningServiceProperties(
        String serviceName,
        SchedulerProperties scheduler,
        ModelProperties model
) {

    public record SchedulerProperties(
            String refreshCron
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
