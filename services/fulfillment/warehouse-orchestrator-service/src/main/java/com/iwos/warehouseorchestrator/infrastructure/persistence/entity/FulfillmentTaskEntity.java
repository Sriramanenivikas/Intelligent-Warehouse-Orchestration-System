package com.iwos.warehouseorchestrator.infrastructure.persistence.entity;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskType;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "fulfillment_tasks", schema = "warehouse_orchestration")
public class FulfillmentTaskEntity {

    @Id
    @Column(name = "fulfillment_task_id", nullable = false, updatable = false)
    private UUID fulfillmentTaskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fulfillment_order_id", nullable = false)
    private FulfillmentOrderEntity fulfillmentOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 16)
    private FulfillmentTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FulfillmentTaskStatus status;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "task_title", nullable = false, length = 128)
    private String taskTitle;

    @Column(name = "task_payload", nullable = false, columnDefinition = "text")
    private String taskPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getFulfillmentTaskId() {
        return fulfillmentTaskId;
    }

    public void setFulfillmentTaskId(UUID fulfillmentTaskId) {
        this.fulfillmentTaskId = fulfillmentTaskId;
    }

    public FulfillmentOrderEntity getFulfillmentOrder() {
        return fulfillmentOrder;
    }

    public void setFulfillmentOrder(FulfillmentOrderEntity fulfillmentOrder) {
        this.fulfillmentOrder = fulfillmentOrder;
    }

    public FulfillmentTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(FulfillmentTaskType taskType) {
        this.taskType = taskType;
    }

    public FulfillmentTaskStatus getStatus() {
        return status;
    }

    public void setStatus(FulfillmentTaskStatus status) {
        this.status = status;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskPayload() {
        return taskPayload;
    }

    public void setTaskPayload(String taskPayload) {
        this.taskPayload = taskPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
