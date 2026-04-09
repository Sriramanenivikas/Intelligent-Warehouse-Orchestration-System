package com.iwos.promiseallocation.infrastructure.persistence.entity;

import com.iwos.promiseallocation.domain.promise.PromiseAllocationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "promise_evaluations", schema = "promise_allocation")
public class PromiseEvaluationEntity {

    @Id
    @Column(name = "evaluation_id", nullable = false, updatable = false)
    private UUID evaluationId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PromiseAllocationStatus status;

    @Column(name = "fulfillment_node_id", length = 64)
    private String fulfillmentNodeId;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "delivery_address_json", nullable = false, columnDefinition = "text")
    private String deliveryAddressJson;

    @Column(name = "requested_items_json", nullable = false, columnDefinition = "text")
    private String requestedItemsJson;

    @Column(name = "item_decisions_json", nullable = false, columnDefinition = "text")
    private String itemDecisionsJson;

    @Column(name = "promised_by")
    private Instant promisedBy;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(UUID evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public PromiseAllocationStatus getStatus() {
        return status;
    }

    public void setStatus(PromiseAllocationStatus status) {
        this.status = status;
    }

    public String getFulfillmentNodeId() {
        return fulfillmentNodeId;
    }

    public void setFulfillmentNodeId(String fulfillmentNodeId) {
        this.fulfillmentNodeId = fulfillmentNodeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public void setDeliveryAddressJson(String deliveryAddressJson) {
        this.deliveryAddressJson = deliveryAddressJson;
    }

    public String getRequestedItemsJson() {
        return requestedItemsJson;
    }

    public void setRequestedItemsJson(String requestedItemsJson) {
        this.requestedItemsJson = requestedItemsJson;
    }

    public String getItemDecisionsJson() {
        return itemDecisionsJson;
    }

    public void setItemDecisionsJson(String itemDecisionsJson) {
        this.itemDecisionsJson = itemDecisionsJson;
    }

    public Instant getPromisedBy() {
        return promisedBy;
    }

    public void setPromisedBy(Instant promisedBy) {
        this.promisedBy = promisedBy;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
