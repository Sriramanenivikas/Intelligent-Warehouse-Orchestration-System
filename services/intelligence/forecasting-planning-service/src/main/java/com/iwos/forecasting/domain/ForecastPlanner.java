package com.iwos.forecasting.domain;

import com.iwos.forecasting.infrastructure.config.ForecastingPlanningServiceProperties;
import com.iwos.forecasting.infrastructure.persistence.entity.InventoryForecastEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ForecastPlanner {

    private final ForecastingPlanningServiceProperties properties;

    public ForecastPlanner(ForecastingPlanningServiceProperties properties) {
        this.properties = properties;
    }

    public InventoryForecastEntity plan(UUID runId, ForecastDemandSnapshot snapshot, Instant generatedAt) {
        BigDecimal hourlyDemand = weightedHourlyDemand(snapshot);
        BigDecimal predicted15m = hourlyDemand.divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP);
        BigDecimal predicted24h = hourlyDemand.multiply(BigDecimal.valueOf(24)).setScale(4, RoundingMode.HALF_UP);
        int available = snapshot.currentOnHandQuantity() - snapshot.currentReservedQuantity();
        BigDecimal daysOfCover = predicted24h.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(999)
                : BigDecimal.valueOf(Math.max(available, 0))
                        .divide(predicted24h, 4, RoundingMode.HALF_UP);
        int targetQuantity = predicted24h
                .multiply(BigDecimal.valueOf(properties.model().leadTimeHours() + properties.model().safetyStockHours()))
                .divide(BigDecimal.valueOf(24), 0, RoundingMode.CEILING)
                .intValue();
        int replenishQty = Math.max(0, targetQuantity - available);
        String risk = stockoutRisk(daysOfCover, available, hourlyDemand);

        InventoryForecastEntity forecast = new InventoryForecastEntity();
        forecast.setForecastId(UUID.randomUUID());
        forecast.setForecastRunId(runId);
        forecast.setNodeId(snapshot.nodeId());
        forecast.setSku(snapshot.sku());
        forecast.setCurrentOnHandQuantity(snapshot.currentOnHandQuantity());
        forecast.setCurrentReservedQuantity(snapshot.currentReservedQuantity());
        forecast.setAvailableQuantity(available);
        forecast.setDemandLast1h(snapshot.demandLast1h().setScale(2, RoundingMode.HALF_UP));
        forecast.setDemandLast6h(snapshot.demandLast6h().setScale(2, RoundingMode.HALF_UP));
        forecast.setDemandLast24h(snapshot.demandLast24h().setScale(2, RoundingMode.HALF_UP));
        forecast.setPredictedHourlyDemand(hourlyDemand.setScale(4, RoundingMode.HALF_UP));
        forecast.setPredicted15mDemand(predicted15m);
        forecast.setPredicted24hDemand(predicted24h);
        forecast.setDaysOfCover(daysOfCover);
        forecast.setStockoutRisk(risk);
        forecast.setRecommendedReplenishmentQuantity(replenishQty);
        forecast.setRecommendedReorder(replenishQty > 0);
        forecast.setModelVersion(properties.model().version());
        forecast.setGeneratedAt(generatedAt);
        return forecast;
    }

    private BigDecimal weightedHourlyDemand(ForecastDemandSnapshot snapshot) {
        BigDecimal last1h = snapshot.demandLast1h();
        BigDecimal last6hRate = snapshot.demandLast6h().divide(BigDecimal.valueOf(6), 4, RoundingMode.HALF_UP);
        BigDecimal last24hRate = snapshot.demandLast24h().divide(BigDecimal.valueOf(24), 4, RoundingMode.HALF_UP);
        return last1h.multiply(BigDecimal.valueOf(0.5))
                .add(last6hRate.multiply(BigDecimal.valueOf(0.3)))
                .add(last24hRate.multiply(BigDecimal.valueOf(0.2)));
    }

    private String stockoutRisk(BigDecimal daysOfCover, int available, BigDecimal hourlyDemand) {
        if (available <= 0 && hourlyDemand.compareTo(BigDecimal.ZERO) > 0) {
            return "CRITICAL";
        }
        if (daysOfCover.compareTo(BigDecimal.valueOf(0.25)) <= 0) {
            return "HIGH";
        }
        if (daysOfCover.compareTo(BigDecimal.ONE) <= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
