package com.iwos.controltower;

import com.iwos.controltower.infrastructure.config.ControlTowerServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ControlTowerServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Control Tower Service API",
        version = "v1",
        description = "Operational snapshot, backlog, and forecast alert APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ControlTowerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlTowerServiceApplication.class, args);
    }
}
