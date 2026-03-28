package com.iwos.orderintake.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "order_intent_items")
public class OrderIntentItemEntity {

    @Id
    @Column(name = "order_intent_item_id", nullable = false, updatable = false)
    private UUID orderIntentItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_intent_id", nullable = false)
    private OrderIntentEntity orderIntent;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getOrderIntentItemId() {
        return orderIntentItemId;
    }

    public void setOrderIntentItemId(UUID orderIntentItemId) {
        this.orderIntentItemId = orderIntentItemId;
    }

    public OrderIntentEntity getOrderIntent() {
        return orderIntent;
    }

    public void setOrderIntent(OrderIntentEntity orderIntent) {
        this.orderIntent = orderIntent;
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
}
