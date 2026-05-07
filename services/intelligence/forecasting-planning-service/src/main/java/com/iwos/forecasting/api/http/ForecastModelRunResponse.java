package com.iwos.forecasting.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ForecastModelRunResponse(
        UUID modelRunId,
        UUID forecastRunId,
        String mlflowRunId,
        String registeredModelName,
        String registeredModelVersion,
        String modelAlias,
        String algorithm,
        String trainingStatus,
        int trainingSampleCount,
        int validationSampleCount,
        int featureCount,
        int predictionHorizonMinutes,
        BigDecimal mae,
        BigDecimal rmse,
        BigDecimal r2,
        String trackingUri,
        String artifactUri,
        Instant trainingStartedAt,
        Instant trainingCompletedAt,
        Instant createdAt
) {
}
