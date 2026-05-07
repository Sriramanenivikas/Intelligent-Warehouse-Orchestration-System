package com.iwos.shipmentnetwork;

import com.iwos.shipmentnetwork.infrastructure.config.ShipmentNetworkServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ShipmentNetworkServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Shipment Network Service API",
        version = "v1",
        description = "Parcel network movement, AWB lookup, and route progression APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ShipmentNetworkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipmentNetworkServiceApplication.class, args);
    }
}
