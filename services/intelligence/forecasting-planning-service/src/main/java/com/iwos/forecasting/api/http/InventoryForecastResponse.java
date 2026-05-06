package com.iwos.forecasting.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryForecastResponse(
        UUID forecastId,
        UUID forecastRunId,
        String nodeId,
        String sku,
        int currentOnHandQuantity,
        int currentReservedQuantity,
        int availableQuantity,
        BigDecimal demandLast1h,
        BigDecimal demandLast6h,
        BigDecimal demandLast24h,
        BigDecimal predictedHourlyDemand,
        BigDecimal predicted15mDemand,
        BigDecimal predicted24hDemand,
        BigDecimal daysOfCover,
        String stockoutRisk,
        int recommendedReplenishmentQuantity,
        boolean recommendedReorder,
        String modelVersion,
        Instant generatedAt
) {
}
