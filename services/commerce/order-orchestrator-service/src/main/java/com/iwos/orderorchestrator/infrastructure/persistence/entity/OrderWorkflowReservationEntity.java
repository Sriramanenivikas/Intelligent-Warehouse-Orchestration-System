package com.iwos.orderorchestrator.infrastructure.persistence.entity;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowReservationStatus;
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
@Table(name = "order_workflow_reservations", schema = "order_orchestration")
public class OrderWorkflowReservationEntity {

    @Id
    @Column(name = "workflow_reservation_id", nullable = false, updatable = false)
    private UUID workflowReservationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private OrderWorkflowEntity workflow;

    @Column(name = "order_intent_item_id", nullable = false)
    private UUID orderIntentItemId;

    @Column(name = "inventory_reservation_id", nullable = false, unique = true)
    private UUID inventoryReservationId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderWorkflowReservationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getWorkflowReservationId() {
        return workflowReservationId;
    }

    public void setWorkflowReservationId(UUID workflowReservationId) {
        this.workflowReservationId = workflowReservationId;
    }

    public OrderWorkflowEntity getWorkflow() {
        return workflow;
    }

    public void setWorkflow(OrderWorkflowEntity workflow) {
        this.workflow = workflow;
    }

    public UUID getOrderIntentItemId() {
        return orderIntentItemId;
    }

    public void setOrderIntentItemId(UUID orderIntentItemId) {
        this.orderIntentItemId = orderIntentItemId;
    }

    public UUID getInventoryReservationId() {
        return inventoryReservationId;
    }

    public void setInventoryReservationId(UUID inventoryReservationId) {
        this.inventoryReservationId = inventoryReservationId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderWorkflowReservationStatus getStatus() {
        return status;
    }

    public void setStatus(OrderWorkflowReservationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
