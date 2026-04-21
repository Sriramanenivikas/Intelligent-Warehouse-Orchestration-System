package com.iwos.shipmentnetwork.infrastructure.persistence.entity;

import com.iwos.shipmentnetwork.domain.network.ScanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scan_events", schema = "shipment_network")
public class ScanEventEntity {

    @Id
    @Column(name = "scan_event_id", nullable = false, updatable = false)
    private UUID scanEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_shipment_id", nullable = false)
    private NetworkShipmentEntity networkShipment;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "awb_number", nullable = false, length = 64)
    private String awbNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false, length = 32)
    private ScanType scanType;

    @Column(name = "node_id", length = 64)
    private String nodeId;

    @Column(name = "facility_code", length = 64)
    private String facilityCode;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getScanEventId() {
        return scanEventId;
    }

    public void setScanEventId(UUID scanEventId) {
        this.scanEventId = scanEventId;
    }

    public NetworkShipmentEntity getNetworkShipment() {
        return networkShipment;
    }

    public void setNetworkShipment(NetworkShipmentEntity networkShipment) {
        this.networkShipment = networkShipment;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getAwbNumber() {
        return awbNumber;
    }

    public void setAwbNumber(String awbNumber) {
        this.awbNumber = awbNumber;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getFacilityCode() {
        return facilityCode;
    }

    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
