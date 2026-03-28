package com.iwos.orderorchestrator.infrastructure.persistence.entity;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowStatus;
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
@Table(name = "order_workflows", schema = "order_orchestration")
public class OrderWorkflowEntity {

    @Id
    @Column(name = "workflow_id", nullable = false, updatable = false)
    private UUID workflowId;

    @Column(name = "order_intent_id", nullable = false, unique = true)
    private UUID orderIntentId;

    @Column(name = "source_outbox_event_id", nullable = false, unique = true)
    private UUID sourceOutboxEventId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "fulfillment_node_id", nullable = false, length = 64)
    private String fulfillmentNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    private OrderWorkflowStatus status;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderWorkflowReservationEntity> reservations = new ArrayList<>();

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

    public UUID getSourceOutboxEventId() {
        return sourceOutboxEventId;
    }

    public void setSourceOutboxEventId(UUID sourceOutboxEventId) {
        this.sourceOutboxEventId = sourceOutboxEventId;
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

    public OrderWorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(OrderWorkflowStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderWorkflowReservationEntity> getReservations() {
        return reservations;
    }

    public void addReservation(OrderWorkflowReservationEntity reservation) {
        reservation.setWorkflow(this);
        this.reservations.add(reservation);
    }
}
