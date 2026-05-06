package com.iwos.forecasting.api.http;

import java.time.Instant;
import java.util.UUID;

public record ForecastRunResponse(
        UUID forecastRunId,
        String modelVersion,
        String triggeredBy,
        String runStatus,
        int forecastCount,
        Instant startedAt,
        Instant completedAt
) {
}
