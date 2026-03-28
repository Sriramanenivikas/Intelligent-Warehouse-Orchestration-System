package com.iwos.orderorchestrator.infrastructure.source.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "SourceOrderIntentItemEntity")
@Table(name = "order_intent_items", schema = "public")
public class SourceOrderIntentItemEntity {

    @Id
    @Column(name = "order_intent_item_id", nullable = false, updatable = false)
    private UUID orderIntentItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_intent_id", nullable = false)
    private SourceOrderIntentEntity orderIntent;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getOrderIntentItemId() {
        return orderIntentItemId;
    }

    public SourceOrderIntentEntity getOrderIntent() {
        return orderIntent;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
