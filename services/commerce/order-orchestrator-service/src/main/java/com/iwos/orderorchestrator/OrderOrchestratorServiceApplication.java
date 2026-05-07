package com.iwos.orderorchestrator;

import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OrderOrchestratorServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Order Orchestrator Service API",
        version = "v1",
        description = "Order orchestration, fulfillment order creation, and downstream coordination APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class OrderOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderOrchestratorServiceApplication.class, args);
    }
}
