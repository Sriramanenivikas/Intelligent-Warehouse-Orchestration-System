package com.iwos.promiseallocation.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "iwos.promise-allocation")
public class PromiseAllocationServiceProperties {

    @NotBlank
    private String inventoryServiceBaseUrl = "http://localhost:8082";

    @NotNull
    private Duration promisedDuration = Duration.ofMinutes(45);

    public String getInventoryServiceBaseUrl() {
        return inventoryServiceBaseUrl;
    }

    public void setInventoryServiceBaseUrl(String inventoryServiceBaseUrl) {
        this.inventoryServiceBaseUrl = inventoryServiceBaseUrl;
    }

    public Duration getPromisedDuration() {
        return promisedDuration;
    }

    public void setPromisedDuration(Duration promisedDuration) {
        this.promisedDuration = promisedDuration;
    }
}
