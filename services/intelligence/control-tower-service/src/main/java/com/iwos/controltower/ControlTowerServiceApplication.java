package com.iwos.controltower;

import com.iwos.controltower.infrastructure.config.ControlTowerServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ControlTowerServiceProperties.class)
public class ControlTowerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlTowerServiceApplication.class, args);
    }
}
