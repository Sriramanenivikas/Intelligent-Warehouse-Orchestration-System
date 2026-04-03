package com.iwos.orderorchestrator;

import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(OrderOrchestratorServiceProperties.class)
@EnableScheduling
public class OrderOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderOrchestratorServiceApplication.class, args);
    }
}
