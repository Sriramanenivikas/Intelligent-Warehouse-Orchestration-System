package com.iwos.orderintake;

import com.iwos.orderintake.infrastructure.config.OrderIntakeServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Bootstraps the order intake service.
 */
@SpringBootApplication
@EnableConfigurationProperties(OrderIntakeServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Order Intake Service API",
        version = "v1",
        description = "Fast order acceptance, idempotency, and intake boundary APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class OrderIntakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderIntakeServiceApplication.class, args);
    }
}
