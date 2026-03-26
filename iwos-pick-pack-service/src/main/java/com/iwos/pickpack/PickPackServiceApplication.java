package com.iwos.pickpack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PickPackServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PickPackServiceApplication.class, args);
    }
}
