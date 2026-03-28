package com.iwos.inventoryledger;

import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InventoryLedgerServiceProperties.class)
public class InventoryLedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryLedgerServiceApplication.class, args);
    }
}
