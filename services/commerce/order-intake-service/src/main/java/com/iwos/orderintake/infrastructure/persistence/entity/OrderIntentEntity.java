package com.iwos.orderintake.infrastructure.persistence.entity;

import com.iwos.orderintake.domain.order.OrderChannel;
import com.iwos.orderintake.domain.order.OrderIntentStatus;
import com.iwos.orderintake.domain.order.PaymentMode;
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
@Table(name = "order_intents")
public class OrderIntentEntity {

    @Id
    @Column(name = "order_intent_id", nullable = false, updatable = false)
    private UUID orderIntentId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private OrderChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 32)
    private PaymentMode paymentMode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "delivery_address_json", nullable = false, columnDefinition = "text")
    private String deliveryAddressJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderIntentStatus status;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "orderIntent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderIntentItemEntity> items = new ArrayList<>();

    public UUID getOrderIntentId() {
        return orderIntentId;
    }

    public void setOrderIntentId(UUID orderIntentId) {
        this.orderIntentId = orderIntentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderChannel getChannel() {
        return channel;
    }

    public void setChannel(OrderChannel channel) {
        this.channel = channel;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public void setDeliveryAddressJson(String deliveryAddressJson) {
        this.deliveryAddressJson = deliveryAddressJson;
    }

    public OrderIntentStatus getStatus() {
        return status;
    }

    public void setStatus(OrderIntentStatus status) {
        this.status = status;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public List<OrderIntentItemEntity> getItems() {
        return items;
    }

    public void addItem(OrderIntentItemEntity item) {
        item.setOrderIntent(this);
        this.items.add(item);
    }
}
