package com.iwos.orderintake;

import com.iwos.orderintake.infrastructure.config.OrderIntakeServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstraps the order intake service.
 */
@SpringBootApplication
@EnableConfigurationProperties(OrderIntakeServiceProperties.class)
@EnableScheduling
public class OrderIntakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderIntakeServiceApplication.class, args);
    }
}
