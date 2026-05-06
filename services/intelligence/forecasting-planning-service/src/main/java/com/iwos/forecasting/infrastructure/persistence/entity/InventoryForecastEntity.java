package com.iwos.forecasting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_forecasts", schema = "forecasting_planning")
public class InventoryForecastEntity {

    @Id
    @Column(name = "forecast_id", nullable = false, updatable = false)
    private UUID forecastId;

    @Column(name = "forecast_run_id", nullable = false)
    private UUID forecastRunId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Column(name = "current_on_hand_quantity", nullable = false)
    private int currentOnHandQuantity;

    @Column(name = "current_reserved_quantity", nullable = false)
    private int currentReservedQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "demand_last_1h", nullable = false, precision = 18, scale = 2)
    private BigDecimal demandLast1h;

    @Column(name = "demand_last_6h", nullable = false, precision = 18, scale = 2)
    private BigDecimal demandLast6h;

    @Column(name = "demand_last_24h", nullable = false, precision = 18, scale = 2)
    private BigDecimal demandLast24h;

    @Column(name = "predicted_hourly_demand", nullable = false, precision = 18, scale = 4)
    private BigDecimal predictedHourlyDemand;

    @Column(name = "predicted_15m_demand", nullable = false, precision = 18, scale = 4)
    private BigDecimal predicted15mDemand;

    @Column(name = "predicted_24h_demand", nullable = false, precision = 18, scale = 4)
    private BigDecimal predicted24hDemand;

    @Column(name = "days_of_cover", nullable = false, precision = 18, scale = 4)
    private BigDecimal daysOfCover;

    @Column(name = "stockout_risk", nullable = false, length = 16)
    private String stockoutRisk;

    @Column(name = "recommended_replenishment_quantity", nullable = false)
    private int recommendedReplenishmentQuantity;

    @Column(name = "recommended_reorder", nullable = false)
    private boolean recommendedReorder;

    @Column(name = "model_version", nullable = false, length = 128)
    private String modelVersion;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public UUID getForecastId() { return forecastId; }
    public void setForecastId(UUID forecastId) { this.forecastId = forecastId; }
    public UUID getForecastRunId() { return forecastRunId; }
    public void setForecastRunId(UUID forecastRunId) { this.forecastRunId = forecastRunId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getCurrentOnHandQuantity() { return currentOnHandQuantity; }
    public void setCurrentOnHandQuantity(int currentOnHandQuantity) { this.currentOnHandQuantity = currentOnHandQuantity; }
    public int getCurrentReservedQuantity() { return currentReservedQuantity; }
    public void setCurrentReservedQuantity(int currentReservedQuantity) { this.currentReservedQuantity = currentReservedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getDemandLast1h() { return demandLast1h; }
    public void setDemandLast1h(BigDecimal demandLast1h) { this.demandLast1h = demandLast1h; }
    public BigDecimal getDemandLast6h() { return demandLast6h; }
    public void setDemandLast6h(BigDecimal demandLast6h) { this.demandLast6h = demandLast6h; }
    public BigDecimal getDemandLast24h() { return demandLast24h; }
    public void setDemandLast24h(BigDecimal demandLast24h) { this.demandLast24h = demandLast24h; }
    public BigDecimal getPredictedHourlyDemand() { return predictedHourlyDemand; }
    public void setPredictedHourlyDemand(BigDecimal predictedHourlyDemand) { this.predictedHourlyDemand = predictedHourlyDemand; }
    public BigDecimal getPredicted15mDemand() { return predicted15mDemand; }
    public void setPredicted15mDemand(BigDecimal predicted15mDemand) { this.predicted15mDemand = predicted15mDemand; }
    public BigDecimal getPredicted24hDemand() { return predicted24hDemand; }
    public void setPredicted24hDemand(BigDecimal predicted24hDemand) { this.predicted24hDemand = predicted24hDemand; }
    public BigDecimal getDaysOfCover() { return daysOfCover; }
    public void setDaysOfCover(BigDecimal daysOfCover) { this.daysOfCover = daysOfCover; }
    public String getStockoutRisk() { return stockoutRisk; }
    public void setStockoutRisk(String stockoutRisk) { this.stockoutRisk = stockoutRisk; }
    public int getRecommendedReplenishmentQuantity() { return recommendedReplenishmentQuantity; }
    public void setRecommendedReplenishmentQuantity(int recommendedReplenishmentQuantity) { this.recommendedReplenishmentQuantity = recommendedReplenishmentQuantity; }
    public boolean isRecommendedReorder() { return recommendedReorder; }
    public void setRecommendedReorder(boolean recommendedReorder) { this.recommendedReorder = recommendedReorder; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
