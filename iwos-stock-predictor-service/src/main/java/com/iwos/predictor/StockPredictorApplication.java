package com.iwos.predictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StockPredictorApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockPredictorApplication.class, args);
    }
}
