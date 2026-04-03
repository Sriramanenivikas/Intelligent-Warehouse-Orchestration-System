package com.iwos.orderorchestrator.infrastructure.source.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "SourceOrderIntentEntity")
@Table(name = "order_intents", schema = "public")
public class SourceOrderIntentEntity {

    @Id
    @Column(name = "order_intent_id", nullable = false, updatable = false)
    private UUID orderIntentId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "payment_mode", nullable = false, length = 32)
    private String paymentMode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "delivery_address_json", nullable = false, columnDefinition = "text")
    private String deliveryAddressJson;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @OneToMany(mappedBy = "orderIntent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SourceOrderIntentItemEntity> items = new ArrayList<>();

    public UUID getOrderIntentId() {
        return orderIntentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public List<SourceOrderIntentItemEntity> getItems() {
        return items;
    }
}
