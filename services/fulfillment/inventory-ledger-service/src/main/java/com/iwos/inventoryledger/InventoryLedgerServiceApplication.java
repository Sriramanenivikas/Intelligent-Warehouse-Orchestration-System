package com.iwos.inventoryledger;

import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(InventoryLedgerServiceProperties.class)
@EnableScheduling
public class InventoryLedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryLedgerServiceApplication.class, args);
    }
}
