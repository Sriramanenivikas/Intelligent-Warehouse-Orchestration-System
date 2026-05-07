package com.iwos.inventoryledger;

import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InventoryLedgerServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Inventory Ledger Service API",
        version = "v1",
        description = "Inventory stock, reservations, and ledger consistency APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class InventoryLedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryLedgerServiceApplication.class, args);
    }
}
