package com.iwos.forecasting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "forecast_model_runs", schema = "forecasting_planning")
public class ForecastModelRunEntity {

    @Id
    @Column(name = "model_run_id", nullable = false, updatable = false)
    private UUID modelRunId;

    @Column(name = "forecast_run_id", nullable = false)
    private UUID forecastRunId;

    @Column(name = "mlflow_run_id", length = 64)
    private String mlflowRunId;

    @Column(name = "registered_model_name", nullable = false, length = 128)
    private String registeredModelName;

    @Column(name = "registered_model_version", length = 32)
    private String registeredModelVersion;

    @Column(name = "model_alias", length = 32)
    private String modelAlias;

    @Column(name = "algorithm", nullable = false, length = 64)
    private String algorithm;

    @Column(name = "training_status", nullable = false, length = 32)
    private String trainingStatus;

    @Column(name = "training_sample_count", nullable = false)
    private int trainingSampleCount;

    @Column(name = "validation_sample_count", nullable = false)
    private int validationSampleCount;

    @Column(name = "feature_count", nullable = false)
    private int featureCount;

    @Column(name = "prediction_horizon_minutes", nullable = false)
    private int predictionHorizonMinutes;

    @Column(name = "mae", precision = 18, scale = 6)
    private BigDecimal mae;

    @Column(name = "rmse", precision = 18, scale = 6)
    private BigDecimal rmse;

    @Column(name = "r2", precision = 18, scale = 6)
    private BigDecimal r2;

    @Column(name = "tracking_uri", length = 256)
    private String trackingUri;

    @Column(name = "artifact_uri", length = 512)
    private String artifactUri;

    @Column(name = "training_started_at", nullable = false)
    private Instant trainingStartedAt;

    @Column(name = "training_completed_at")
    private Instant trainingCompletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getModelRunId() {
        return modelRunId;
    }

    public void setModelRunId(UUID modelRunId) {
        this.modelRunId = modelRunId;
    }

    public UUID getForecastRunId() {
        return forecastRunId;
    }

    public void setForecastRunId(UUID forecastRunId) {
        this.forecastRunId = forecastRunId;
    }

    public String getMlflowRunId() {
        return mlflowRunId;
    }

    public void setMlflowRunId(String mlflowRunId) {
        this.mlflowRunId = mlflowRunId;
    }

    public String getRegisteredModelName() {
        return registeredModelName;
    }

    public void setRegisteredModelName(String registeredModelName) {
        this.registeredModelName = registeredModelName;
    }

    public String getRegisteredModelVersion() {
        return registeredModelVersion;
    }

    public void setRegisteredModelVersion(String registeredModelVersion) {
        this.registeredModelVersion = registeredModelVersion;
    }

    public String getModelAlias() {
        return modelAlias;
    }

    public void setModelAlias(String modelAlias) {
        this.modelAlias = modelAlias;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getTrainingStatus() {
        return trainingStatus;
    }

    public void setTrainingStatus(String trainingStatus) {
        this.trainingStatus = trainingStatus;
    }

    public int getTrainingSampleCount() {
        return trainingSampleCount;
    }

    public void setTrainingSampleCount(int trainingSampleCount) {
        this.trainingSampleCount = trainingSampleCount;
    }

    public int getValidationSampleCount() {
        return validationSampleCount;
    }

    public void setValidationSampleCount(int validationSampleCount) {
        this.validationSampleCount = validationSampleCount;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public int getPredictionHorizonMinutes() {
        return predictionHorizonMinutes;
    }

    public void setPredictionHorizonMinutes(int predictionHorizonMinutes) {
        this.predictionHorizonMinutes = predictionHorizonMinutes;
    }

    public BigDecimal getMae() {
        return mae;
    }

    public void setMae(BigDecimal mae) {
        this.mae = mae;
    }

    public BigDecimal getRmse() {
        return rmse;
    }

    public void setRmse(BigDecimal rmse) {
        this.rmse = rmse;
    }

    public BigDecimal getR2() {
        return r2;
    }

    public void setR2(BigDecimal r2) {
        this.r2 = r2;
    }

    public String getTrackingUri() {
        return trackingUri;
    }

    public void setTrackingUri(String trackingUri) {
        this.trackingUri = trackingUri;
    }

    public String getArtifactUri() {
        return artifactUri;
    }

    public void setArtifactUri(String artifactUri) {
        this.artifactUri = artifactUri;
    }

    public Instant getTrainingStartedAt() {
        return trainingStartedAt;
    }

    public void setTrainingStartedAt(Instant trainingStartedAt) {
        this.trainingStartedAt = trainingStartedAt;
    }

    public Instant getTrainingCompletedAt() {
        return trainingCompletedAt;
    }

    public void setTrainingCompletedAt(Instant trainingCompletedAt) {
        this.trainingCompletedAt = trainingCompletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
