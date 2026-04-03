package com.iwos.orderorchestrator;

import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OrderOrchestratorServiceProperties.class)
public class OrderOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderOrchestratorServiceApplication.class, args);
    }
}
