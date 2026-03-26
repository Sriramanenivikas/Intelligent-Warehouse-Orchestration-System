package com.iwos.returns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.iwos.returns", "com.iwos.common"})
@EnableDiscoveryClient @EnableFeignClients
public class ReturnsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReturnsServiceApplication.class, args);
    }
}
