package com.iwos.forecasting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "forecast_runs", schema = "forecasting_planning")
public class ForecastRunEntity {

    @Id
    @Column(name = "forecast_run_id", nullable = false, updatable = false)
    private UUID forecastRunId;

    @Column(name = "model_version", nullable = false, length = 128)
    private String modelVersion;

    @Column(name = "triggered_by", nullable = false, length = 64)
    private String triggeredBy;

    @Column(name = "run_status", nullable = false, length = 32)
    private String runStatus;

    @Column(name = "forecast_count", nullable = false)
    private int forecastCount;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public UUID getForecastRunId() { return forecastRunId; }
    public void setForecastRunId(UUID forecastRunId) { this.forecastRunId = forecastRunId; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getRunStatus() { return runStatus; }
    public void setRunStatus(String runStatus) { this.runStatus = runStatus; }
    public int getForecastCount() { return forecastCount; }
    public void setForecastCount(int forecastCount) { this.forecastCount = forecastCount; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
