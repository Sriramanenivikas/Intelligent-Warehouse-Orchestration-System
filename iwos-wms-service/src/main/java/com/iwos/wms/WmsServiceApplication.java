package com.iwos.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WmsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsServiceApplication.class, args);
    }
}
