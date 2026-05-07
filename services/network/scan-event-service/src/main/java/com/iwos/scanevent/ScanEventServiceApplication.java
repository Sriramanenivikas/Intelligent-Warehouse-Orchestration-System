package com.iwos.scanevent;

import com.iwos.scanevent.infrastructure.config.ScanEventServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ScanEventServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Scan Event Service API",
        version = "v1",
        description = "Shipment scan normalization, tracking event, and shipment timeline APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ScanEventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanEventServiceApplication.class, args);
    }
}
