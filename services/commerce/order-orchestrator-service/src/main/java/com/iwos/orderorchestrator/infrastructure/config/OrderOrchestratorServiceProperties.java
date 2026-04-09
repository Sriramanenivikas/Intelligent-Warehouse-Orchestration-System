package com.iwos.orderorchestrator.infrastructure.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "iwos.order-orchestrator")
public class OrderOrchestratorServiceProperties {

    @NotBlank
    private String inventoryServiceBaseUrl = "http://localhost:8082";

    @NotBlank
    private String promiseServiceBaseUrl = "http://localhost:8084";

    @NotBlank
    private String paymentServiceBaseUrl = "http://localhost:8085";

    @NotBlank
    private String defaultFulfillmentNodeId = "NODE-DELHI-01";

    @Min(1)
    @Max(100)
    private int pendingFetchLimit = 25;

    public String getInventoryServiceBaseUrl() {
        return inventoryServiceBaseUrl;
    }

    public void setInventoryServiceBaseUrl(String inventoryServiceBaseUrl) {
        this.inventoryServiceBaseUrl = inventoryServiceBaseUrl;
    }

    public String getPromiseServiceBaseUrl() {
        return promiseServiceBaseUrl;
    }

    public void setPromiseServiceBaseUrl(String promiseServiceBaseUrl) {
        this.promiseServiceBaseUrl = promiseServiceBaseUrl;
    }

    public String getPaymentServiceBaseUrl() {
        return paymentServiceBaseUrl;
    }

    public void setPaymentServiceBaseUrl(String paymentServiceBaseUrl) {
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    public String getDefaultFulfillmentNodeId() {
        return defaultFulfillmentNodeId;
    }

    public void setDefaultFulfillmentNodeId(String defaultFulfillmentNodeId) {
        this.defaultFulfillmentNodeId = defaultFulfillmentNodeId;
    }

    public int getPendingFetchLimit() {
        return pendingFetchLimit;
    }

    public void setPendingFetchLimit(int pendingFetchLimit) {
        this.pendingFetchLimit = pendingFetchLimit;
    }
}
