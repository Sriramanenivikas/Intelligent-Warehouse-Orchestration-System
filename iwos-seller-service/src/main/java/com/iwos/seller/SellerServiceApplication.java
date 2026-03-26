package com.iwos.seller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.iwos.seller", "com.iwos.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class SellerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SellerServiceApplication.class, args);
    }
}
