package com.iwos.loadtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ORDER GENERATOR - LOAD TESTING SERVICE
 *
 * Generates realistic orders at 10,000 orders/second
 * Used for load testing IWOS system under peak traffic
 *
 * Interview talking point:
 * "I built a custom order generator that can produce 10K orders/second
 *  with realistic Indian pincodes and geolocation data for load testing"
 */
@SpringBootApplication
@EnableScheduling
public class OrderGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderGeneratorApplication.class, args);
    }
}
