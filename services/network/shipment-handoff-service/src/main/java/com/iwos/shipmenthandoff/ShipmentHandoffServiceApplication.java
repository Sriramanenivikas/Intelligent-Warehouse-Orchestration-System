package com.iwos.shipmenthandoff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@OpenAPIDefinition(info = @Info(
        title = "IWOS Shipment Handoff Service API",
        version = "v1",
        description = "Shipment creation, manifesting, and dispatch handoff APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ShipmentHandoffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipmentHandoffServiceApplication.class, args);
    }
}
