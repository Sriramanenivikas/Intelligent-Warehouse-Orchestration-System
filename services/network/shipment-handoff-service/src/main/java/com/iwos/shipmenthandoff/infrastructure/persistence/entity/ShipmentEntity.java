package com.iwos.shipmenthandoff.infrastructure.persistence.entity;

import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
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
@Table(name = "shipments", schema = "shipment_handoff")
public class ShipmentEntity {

    @Id
    @Column(name = "shipment_id", nullable = false, updatable = false)
    private UUID shipmentId;

    @Column(name = "fulfillment_order_id", nullable = false, unique = true)
    private UUID fulfillmentOrderId;

    @Column(name = "order_intent_id", nullable = false)
    private UUID orderIntentId;

    @Column(name = "workflow_id")
    private UUID workflowId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "awb_number", length = 64)
    private String awbNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_code", nullable = false, length = 32)
    private CarrierCode carrierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ShipmentStatus status;

    @Column(name = "origin_node_id", nullable = false, length = 64)
    private String originNodeId;

    @Column(name = "destination_name", length = 128)
    private String destinationName;

    @Column(name = "destination_phone", length = 20)
    private String destinationPhone;

    @Column(name = "destination_line1", length = 255)
    private String destinationLine1;

    @Column(name = "destination_line2", length = 255)
    private String destinationLine2;

    @Column(name = "destination_city", length = 64)
    private String destinationCity;

    @Column(name = "destination_state", length = 64)
    private String destinationState;

    @Column(name = "destination_postal_code", length = 16)
    private String destinationPostalCode;

    @Column(name = "destination_country", length = 3)
    private String destinationCountry;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "package_count")
    private Integer packageCount;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "manifested_at")
    private Instant manifestedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public ShipmentEntity() {}

    // Getters and setters
    public UUID getShipmentId() { return shipmentId; }
    public void setShipmentId(UUID shipmentId) { this.shipmentId = shipmentId; }

    public UUID getFulfillmentOrderId() { return fulfillmentOrderId; }
    public void setFulfillmentOrderId(UUID fulfillmentOrderId) { this.fulfillmentOrderId = fulfillmentOrderId; }

    public UUID getOrderIntentId() { return orderIntentId; }
    public void setOrderIntentId(UUID orderIntentId) { this.orderIntentId = orderIntentId; }

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }

    public CarrierCode getCarrierCode() { return carrierCode; }
    public void setCarrierCode(CarrierCode carrierCode) { this.carrierCode = carrierCode; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public String getOriginNodeId() { return originNodeId; }
    public void setOriginNodeId(String originNodeId) { this.originNodeId = originNodeId; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getDestinationPhone() { return destinationPhone; }
    public void setDestinationPhone(String destinationPhone) { this.destinationPhone = destinationPhone; }

    public String getDestinationLine1() { return destinationLine1; }
    public void setDestinationLine1(String destinationLine1) { this.destinationLine1 = destinationLine1; }

    public String getDestinationLine2() { return destinationLine2; }
    public void setDestinationLine2(String destinationLine2) { this.destinationLine2 = destinationLine2; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public String getDestinationState() { return destinationState; }
    public void setDestinationState(String destinationState) { this.destinationState = destinationState; }

    public String getDestinationPostalCode() { return destinationPostalCode; }
    public void setDestinationPostalCode(String destinationPostalCode) { this.destinationPostalCode = destinationPostalCode; }

    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }

    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }

    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }

    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public void setEstimatedDeliveryAt(Instant estimatedDeliveryAt) { this.estimatedDeliveryAt = estimatedDeliveryAt; }

    public Instant getManifestedAt() { return manifestedAt; }
    public void setManifestedAt(Instant manifestedAt) { this.manifestedAt = manifestedAt; }

    public Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Instant dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
