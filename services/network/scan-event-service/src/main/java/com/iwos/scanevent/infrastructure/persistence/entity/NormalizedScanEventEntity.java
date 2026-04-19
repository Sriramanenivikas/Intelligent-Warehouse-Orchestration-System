package com.iwos.scanevent.infrastructure.persistence.entity;

import com.iwos.scanevent.domain.scan.ScanMilestone;
import com.iwos.scanevent.domain.scan.TrackingStatus;
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
@Table(name = "normalized_scan_events", schema = "scan_event")
public class NormalizedScanEventEntity {

    @Id
    @Column(name = "normalized_scan_event_id", nullable = false, updatable = false)
    private UUID normalizedScanEventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tracked_shipment_id", nullable = false)
    private TrackedShipmentEntity trackedShipment;

    @Column(name = "scan_event_id", nullable = false, unique = true)
    private UUID scanEventId;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "order_intent_id", nullable = false)
    private UUID orderIntentId;

    @Column(name = "awb_number", nullable = false, length = 64)
    private String awbNumber;

    @Column(name = "source_event_type", nullable = false, length = 128)
    private String sourceEventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false, length = 32)
    private ScanMilestone scanType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after_event", nullable = false, length = 32)
    private TrackingStatus statusAfterEvent;

    @Column(name = "node_id", length = 64)
    private String nodeId;

    @Column(name = "facility_code", length = 64)
    private String facilityCode;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    public UUID getNormalizedScanEventId() {
        return normalizedScanEventId;
    }

    public void setNormalizedScanEventId(UUID normalizedScanEventId) {
        this.normalizedScanEventId = normalizedScanEventId;
    }

    public TrackedShipmentEntity getTrackedShipment() {
        return trackedShipment;
    }

    public void setTrackedShipment(TrackedShipmentEntity trackedShipment) {
        this.trackedShipment = trackedShipment;
    }

    public UUID getScanEventId() {
        return scanEventId;
    }

    public void setScanEventId(UUID scanEventId) {
        this.scanEventId = scanEventId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public UUID getOrderIntentId() {
        return orderIntentId;
    }

    public void setOrderIntentId(UUID orderIntentId) {
        this.orderIntentId = orderIntentId;
    }

    public String getAwbNumber() {
        return awbNumber;
    }

    public void setAwbNumber(String awbNumber) {
        this.awbNumber = awbNumber;
    }

    public String getSourceEventType() {
        return sourceEventType;
    }

    public void setSourceEventType(String sourceEventType) {
        this.sourceEventType = sourceEventType;
    }

    public ScanMilestone getScanType() {
        return scanType;
    }

    public void setScanType(ScanMilestone scanType) {
        this.scanType = scanType;
    }

    public TrackingStatus getStatusAfterEvent() {
        return statusAfterEvent;
    }

    public void setStatusAfterEvent(TrackingStatus statusAfterEvent) {
        this.statusAfterEvent = statusAfterEvent;
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

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
