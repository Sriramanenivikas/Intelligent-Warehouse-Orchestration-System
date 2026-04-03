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
    private String defaultFulfillmentNodeId = "NODE-DELHI-01";

    @Min(1)
    @Max(100)
    private int pendingFetchLimit = 25;

    private KafkaProperties kafka = new KafkaProperties();

    public String getInventoryServiceBaseUrl() {
        return inventoryServiceBaseUrl;
    }

    public void setInventoryServiceBaseUrl(String inventoryServiceBaseUrl) {
        this.inventoryServiceBaseUrl = inventoryServiceBaseUrl;
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

    public KafkaProperties getKafka() {
        return kafka;
    }

    public void setKafka(KafkaProperties kafka) {
        this.kafka = kafka;
    }

    public static class KafkaProperties {
        private String orderIntakeTopic = "iwos.order-intake.accepted.v1";
        private String outboxTopic = "iwos.order-orchestrator.events.v1";

        public String getOrderIntakeTopic() {
            return orderIntakeTopic;
        }

        public void setOrderIntakeTopic(String orderIntakeTopic) {
            this.orderIntakeTopic = orderIntakeTopic;
        }

        public String getOutboxTopic() {
            return outboxTopic;
        }

        public void setOutboxTopic(String outboxTopic) {
            this.outboxTopic = outboxTopic;
        }
    }
}
