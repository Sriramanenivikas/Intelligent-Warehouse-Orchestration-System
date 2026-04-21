package com.iwos.scanevent;

import com.iwos.scanevent.infrastructure.config.ScanEventServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ScanEventServiceProperties.class)
public class ScanEventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanEventServiceApplication.class, args);
    }
}
