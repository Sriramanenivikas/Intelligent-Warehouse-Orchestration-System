package com.iwos.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FraudDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }
}
