package com.iwos.warehouseorchestrator;

import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(WarehouseOrchestratorServiceProperties.class)
public class WarehouseOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseOrchestratorServiceApplication.class, args);
    }
}

