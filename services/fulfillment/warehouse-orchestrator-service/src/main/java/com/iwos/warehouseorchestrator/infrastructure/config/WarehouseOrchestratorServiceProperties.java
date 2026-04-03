package com.iwos.warehouseorchestrator.infrastructure.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "iwos.warehouse-orchestrator")
public class WarehouseOrchestratorServiceProperties {

    @NotBlank
    private String orderOrchestratorBaseUrl = "http://localhost:8083";

    @NotBlank
    private String warehouseCode = "DELHI-FC-01";

    @NotBlank
    private String defaultFulfillmentNodeId = "NODE-DELHI-01";

    @Min(1)
    @Max(500)
    private int pickBatchSize = 250;

    private KafkaProperties kafka = new KafkaProperties();

    public String getOrderOrchestratorBaseUrl() {
        return orderOrchestratorBaseUrl;
    }

    public void setOrderOrchestratorBaseUrl(String orderOrchestratorBaseUrl) {
        this.orderOrchestratorBaseUrl = orderOrchestratorBaseUrl;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getDefaultFulfillmentNodeId() {
        return defaultFulfillmentNodeId;
    }

    public void setDefaultFulfillmentNodeId(String defaultFulfillmentNodeId) {
        this.defaultFulfillmentNodeId = defaultFulfillmentNodeId;
    }

    public int getPickBatchSize() {
        return pickBatchSize;
    }

    public void setPickBatchSize(int pickBatchSize) {
        this.pickBatchSize = pickBatchSize;
    }

    public KafkaProperties getKafka() {
        return kafka;
    }

    public void setKafka(KafkaProperties kafka) {
        this.kafka = kafka;
    }

    public static class KafkaProperties {
        private String inboundTopic = "iwos.order-orchestrator.events.v1";
        private String outboxTopic = "iwos.warehouse-orchestrator.events.v1";
        private String outboxPollInterval = "PT5S";

        @Min(1)
        @Max(500)
        private int outboxBatchSize = 50;

        public String getInboundTopic() {
            return inboundTopic;
        }

        public void setInboundTopic(String inboundTopic) {
            this.inboundTopic = inboundTopic;
        }

        public String getOutboxTopic() {
            return outboxTopic;
        }

        public void setOutboxTopic(String outboxTopic) {
            this.outboxTopic = outboxTopic;
        }

        public String getOutboxPollInterval() {
            return outboxPollInterval;
        }

        public void setOutboxPollInterval(String outboxPollInterval) {
            this.outboxPollInterval = outboxPollInterval;
        }

        public int getOutboxBatchSize() {
            return outboxBatchSize;
        }

        public void setOutboxBatchSize(int outboxBatchSize) {
            this.outboxBatchSize = outboxBatchSize;
        }
    }
}
