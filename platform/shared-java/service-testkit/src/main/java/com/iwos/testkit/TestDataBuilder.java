package com.iwos.testkit;

import java.time.Instant;
import java.util.UUID;

/**
 * Builder for creating test data objects.
 * Provides fluent API for test fixture creation.
 */
public class TestDataBuilder {

    private String orderId = UUID.randomUUID().toString();
    private String customerId = "cust-" + UUID.randomUUID().toString().substring(0, 8);
    private String sku = "SKU" + System.currentTimeMillis();
    private Integer quantity = 1;
    private Double price = 100.0;
    private String status = "CREATED";
    private Instant timestamp = Instant.now();

    public TestDataBuilder withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public TestDataBuilder withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public TestDataBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }

    public TestDataBuilder withQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public TestDataBuilder withPrice(Double price) {
        this.price = price;
        return this;
    }

    public TestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public TestDataBuilder withTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String buildOrderId() {
        return orderId;
    }

    public String buildCustomerId() {
        return customerId;
    }

    public String buildSku() {
        return sku;
    }

    public Integer buildQuantity() {
        return quantity;
    }

    public Double buildPrice() {
        return price;
    }

    public String buildStatus() {
        return status;
    }

    public Instant buildTimestamp() {
        return timestamp;
    }
}
