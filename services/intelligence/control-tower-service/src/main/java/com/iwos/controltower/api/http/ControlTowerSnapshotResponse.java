package com.iwos.controltower.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ControlTowerSnapshotResponse(
        UUID controlTowerSnapshotId,
        String snapshotType,
        String modelVersion,
        Instant generatedAt,
        ForecastKpiResponse forecastKpi,
        List<BucketCountResponse> orderIntentsByStatus,
        List<BucketCountResponse> fulfillmentOrdersByStatus,
        List<BucketCountResponse> shipmentsByStatus,
        List<BucketCountResponse> networkShipmentsByStatus,
        List<BucketCountResponse> scanEventsByType,
        List<BucketCountResponse> notificationsByAudience,
        List<BucketCountResponse> notificationsByStatus,
        List<ControlTowerForecastAlertResponse> topForecasts,
        List<ControlTowerExceptionResponse> recentExceptions
) {

    public record ForecastKpiResponse(
            int totalForecasts,
            long criticalCount,
            long highCount,
            long mediumCount,
            long lowCount,
            int totalRecommendedReplenishmentQuantity
    ) {
    }

    public record BucketCountResponse(
            String key,
            long count
    ) {
    }

    public record ControlTowerForecastAlertResponse(
            UUID forecastId,
            UUID forecastRunId,
            String nodeId,
            String sku,
            BigDecimal availableQuantity,
            BigDecimal predicted15mDemand,
            BigDecimal predicted24hDemand,
            BigDecimal daysOfCover,
            String stockoutRisk,
            int recommendedReplenishmentQuantity
    ) {
    }

    public record ControlTowerExceptionResponse(
            UUID shipmentId,
            UUID orderIntentId,
            String awbNumber,
            String currentStatus,
            String lastScanType,
            Instant occurredAt,
            String notes
    ) {
    }
}
