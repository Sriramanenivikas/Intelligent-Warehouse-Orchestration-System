package com.iwos.shipmentnetwork.infrastructure.persistence.entity;

import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStatus;
import com.iwos.shipmentnetwork.domain.network.ScanType;
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
@Table(name = "network_shipments", schema = "shipment_network")
public class NetworkShipmentEntity {

    @Id
    @Column(name = "network_shipment_id", nullable = false, updatable = false)
    private UUID networkShipmentId;

    @Column(name = "shipment_id", nullable = false, unique = true)
    private UUID shipmentId;

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

    @Column(name = "origin_node_id", length = 64)
    private String originNodeId;

    @Column(name = "current_node_id", length = 64)
    private String currentNodeId;

    @Column(name = "current_facility_code", length = 64)
    private String currentFacilityCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private NetworkShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_scan_type", length = 32)
    private ScanType lastScanType;

    @Column(name = "last_scanned_at")
    private Instant lastScannedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getNetworkShipmentId() {
        return networkShipmentId;
    }

    public void setNetworkShipmentId(UUID networkShipmentId) {
        this.networkShipmentId = networkShipmentId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
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

    public String getOriginNodeId() {
        return originNodeId;
    }

    public void setOriginNodeId(String originNodeId) {
        this.originNodeId = originNodeId;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentFacilityCode() {
        return currentFacilityCode;
    }

    public void setCurrentFacilityCode(String currentFacilityCode) {
        this.currentFacilityCode = currentFacilityCode;
    }

    public NetworkShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(NetworkShipmentStatus status) {
        this.status = status;
    }

    public ScanType getLastScanType() {
        return lastScanType;
    }

    public void setLastScanType(ScanType lastScanType) {
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
}
