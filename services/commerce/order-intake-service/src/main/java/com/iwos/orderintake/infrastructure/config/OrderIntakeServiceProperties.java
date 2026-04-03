package com.iwos.orderintake.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwos.order-intake")
public class OrderIntakeServiceProperties {

    private Duration idempotencyCacheTtl = Duration.ofHours(24);

    public Duration getIdempotencyCacheTtl() {
        return idempotencyCacheTtl;
    }

    public void setIdempotencyCacheTtl(Duration idempotencyCacheTtl) {
        this.idempotencyCacheTtl = idempotencyCacheTtl;
    }
}
