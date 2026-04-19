package com.iwos.shipmentnetwork;

import com.iwos.shipmentnetwork.infrastructure.config.ShipmentNetworkServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ShipmentNetworkServiceProperties.class)
public class ShipmentNetworkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipmentNetworkServiceApplication.class, args);
    }
}
