package com.iwos.forecasting.api.http;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ForecastSummaryResponse(
        UUID forecastRunId,
        String modelVersion,
        String runStatus,
        int totalForecasts,
        long criticalCount,
        long highCount,
        long mediumCount,
        long lowCount,
        int totalRecommendedReplenishmentQuantity,
        Instant generatedAt,
        List<InventoryForecastResponse> topReplenishmentForecasts
) {
}
