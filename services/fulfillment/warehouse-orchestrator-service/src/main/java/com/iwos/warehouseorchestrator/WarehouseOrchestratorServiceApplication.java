package com.iwos.warehouseorchestrator;

import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(WarehouseOrchestratorServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Warehouse Orchestrator Service API",
        version = "v1",
        description = "Warehouse fulfillment orchestration, task creation, and state transition APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class WarehouseOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseOrchestratorServiceApplication.class, args);
    }
}
