package com.iwos.controltower.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "control_tower_snapshots", schema = "control_tower")
public class ControlTowerSnapshotEntity {

    @Id
    @Column(name = "control_tower_snapshot_id", nullable = false, updatable = false)
    private UUID controlTowerSnapshotId;

    @Column(name = "snapshot_type", nullable = false, length = 32)
    private String snapshotType;

    @Column(name = "model_version", nullable = false, length = 128)
    private String modelVersion;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public UUID getControlTowerSnapshotId() { return controlTowerSnapshotId; }
    public void setControlTowerSnapshotId(UUID controlTowerSnapshotId) { this.controlTowerSnapshotId = controlTowerSnapshotId; }
    public String getSnapshotType() { return snapshotType; }
    public void setSnapshotType(String snapshotType) { this.snapshotType = snapshotType; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
