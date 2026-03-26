package com.iwos.darkstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.iwos.darkstore", "com.iwos.common"})
@EnableDiscoveryClient @EnableFeignClients @EnableScheduling
public class DarkStoreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DarkStoreServiceApplication.class, args);
    }
}
