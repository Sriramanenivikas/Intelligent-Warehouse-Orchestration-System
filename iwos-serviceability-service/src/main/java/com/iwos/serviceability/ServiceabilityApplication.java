package com.iwos.serviceability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.iwos.serviceability", "com.iwos.common"})
@EnableDiscoveryClient
public class ServiceabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceabilityApplication.class, args);
    }
}
