package com.iwos.warehouseorchestrator.infrastructure.persistence.entity;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "fulfillment_orders", schema = "warehouse_orchestration")
public class FulfillmentOrderEntity {

    @Id
    @Column(name = "fulfillment_order_id", nullable = false, updatable = false)
    private UUID fulfillmentOrderId;

    @Column(name = "workflow_id", nullable = false, unique = true)
    private UUID workflowId;

    @Column(name = "order_intent_id", nullable = false, unique = true)
    private UUID orderIntentId;

    @Column(name = "payment_intent_id", nullable = false)
    private UUID paymentIntentId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "fulfillment_node_id", nullable = false, length = 64)
    private String fulfillmentNodeId;

    @Column(name = "warehouse_code", nullable = false, length = 64)
    private String warehouseCode;

    @Column(name = "source_topic", nullable = false, length = 128)
    private String sourceTopic;

    @Column(name = "source_message_key", nullable = false, unique = true, length = 128)
    private String sourceMessageKey;

    @Column(name = "source_event_type", nullable = false, length = 128)
    private String sourceEventType;

    @Column(name = "source_event_occurred_at", nullable = false)
    private Instant sourceEventOccurredAt;

    @Column(name = "source_event_payload", nullable = false, columnDefinition = "text")
    private String sourceEventPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FulfillmentOrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "fulfillmentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FulfillmentTaskEntity> tasks = new ArrayList<>();

    public UUID getFulfillmentOrderId() {
        return fulfillmentOrderId;
    }

    public void setFulfillmentOrderId(UUID fulfillmentOrderId) {
        this.fulfillmentOrderId = fulfillmentOrderId;
    }

    public UUID getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
    }

    public UUID getOrderIntentId() {
        return orderIntentId;
    }

    public void setOrderIntentId(UUID orderIntentId) {
        this.orderIntentId = orderIntentId;
    }

    public UUID getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(UUID paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFulfillmentNodeId() {
        return fulfillmentNodeId;
    }

    public void setFulfillmentNodeId(String fulfillmentNodeId) {
        this.fulfillmentNodeId = fulfillmentNodeId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public void setSourceTopic(String sourceTopic) {
        this.sourceTopic = sourceTopic;
    }

    public String getSourceMessageKey() {
        return sourceMessageKey;
    }

    public void setSourceMessageKey(String sourceMessageKey) {
        this.sourceMessageKey = sourceMessageKey;
    }

    public String getSourceEventType() {
        return sourceEventType;
    }

    public void setSourceEventType(String sourceEventType) {
        this.sourceEventType = sourceEventType;
    }

    public Instant getSourceEventOccurredAt() {
        return sourceEventOccurredAt;
    }

    public void setSourceEventOccurredAt(Instant sourceEventOccurredAt) {
        this.sourceEventOccurredAt = sourceEventOccurredAt;
    }

    public String getSourceEventPayload() {
        return sourceEventPayload;
    }

    public void setSourceEventPayload(String sourceEventPayload) {
        this.sourceEventPayload = sourceEventPayload;
    }

    public FulfillmentOrderStatus getStatus() {
        return status;
    }

    public void setStatus(FulfillmentOrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<FulfillmentTaskEntity> getTasks() {
        return tasks;
    }

    public void addTask(FulfillmentTaskEntity task) {
        task.setFulfillmentOrder(this);
        tasks.add(task);
    }
}
