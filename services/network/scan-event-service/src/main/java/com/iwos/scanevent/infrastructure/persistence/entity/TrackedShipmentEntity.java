package com.iwos.scanevent.infrastructure.persistence.entity;

import com.iwos.scanevent.domain.scan.ScanMilestone;
import com.iwos.scanevent.domain.scan.TrackingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracked_shipments", schema = "scan_event")
public class TrackedShipmentEntity {

    @Id
    @Column(name = "tracked_shipment_id", nullable = false, updatable = false)
    private UUID trackedShipmentId;

    @Column(name = "shipment_id", nullable = false, unique = true)
    private UUID shipmentId;

    @Column(name = "network_shipment_id")
    private UUID networkShipmentId;

    @Column(name = "fulfillment_order_id", nullable = false)
    private UUID fulfillmentOrderId;

    @Column(name = "order_intent_id", nullable = false)
    private UUID orderIntentId;

    @Column(name = "awb_number", nullable = false, length = 64)
    private String awbNumber;

    @Column(name = "carrier_code", nullable = false, length = 32)
    private String carrierCode;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 32)
    private TrackingStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_scan_type", nullable = false, length = 32)
    private ScanMilestone lastScanType;

    @Column(name = "last_scanned_at", nullable = false)
    private Instant lastScannedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getTrackedShipmentId() {
        return trackedShipmentId;
    }

    public void setTrackedShipmentId(UUID trackedShipmentId) {
        this.trackedShipmentId = trackedShipmentId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public UUID getNetworkShipmentId() {
        return networkShipmentId;
    }

    public void setNetworkShipmentId(UUID networkShipmentId) {
        this.networkShipmentId = networkShipmentId;
    }

    public UUID getFulfillmentOrderId() {
        return fulfillmentOrderId;
    }

    public void setFulfillmentOrderId(UUID fulfillmentOrderId) {
        this.fulfillmentOrderId = fulfillmentOrderId;
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

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public TrackingStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(TrackingStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public ScanMilestone getLastScanType() {
        return lastScanType;
    }

    public void setLastScanType(ScanMilestone lastScanType) {
        this.lastScanType = lastScanType;
    }

    public Instant getLastScannedAt() {
        return lastScannedAt;
    }

    public void setLastScannedAt(Instant lastScannedAt) {
        this.lastScannedAt = lastScannedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
