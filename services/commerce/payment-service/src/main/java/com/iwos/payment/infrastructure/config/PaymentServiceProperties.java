package com.iwos.payment.infrastructure.config;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwos.payment")
public class PaymentServiceProperties {

    private Duration idempotencyCacheTtl = Duration.ofHours(24);
    private String outboxTopic = "iwos.payment.events.v1";
    private Duration outboxPollInterval = Duration.ofSeconds(5);
    private int outboxBatchSize = 50;
    private String providerName = "LOCAL_SIMULATED_GATEWAY";
    private BigDecimal authorizationLimit = new BigDecimal("5000.00");

    public Duration getIdempotencyCacheTtl() {
        return idempotencyCacheTtl;
    }

    public void setIdempotencyCacheTtl(Duration idempotencyCacheTtl) {
        this.idempotencyCacheTtl = idempotencyCacheTtl;
    }

    public String getOutboxTopic() {
        return outboxTopic;
    }

    public void setOutboxTopic(String outboxTopic) {
        this.outboxTopic = outboxTopic;
    }

    public Duration getOutboxPollInterval() {
        return outboxPollInterval;
    }

    public void setOutboxPollInterval(Duration outboxPollInterval) {
        this.outboxPollInterval = outboxPollInterval;
    }

    public int getOutboxBatchSize() {
        return outboxBatchSize;
    }

    public void setOutboxBatchSize(int outboxBatchSize) {
        this.outboxBatchSize = outboxBatchSize;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public BigDecimal getAuthorizationLimit() {
        return authorizationLimit;
    }

    public void setAuthorizationLimit(BigDecimal authorizationLimit) {
        this.authorizationLimit = authorizationLimit;
    }
}
