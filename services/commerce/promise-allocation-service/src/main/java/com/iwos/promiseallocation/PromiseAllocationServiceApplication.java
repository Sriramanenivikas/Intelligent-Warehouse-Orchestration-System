package com.iwos.promiseallocation;

import com.iwos.promiseallocation.infrastructure.config.PromiseAllocationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PromiseAllocationServiceProperties.class)
public class PromiseAllocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromiseAllocationServiceApplication.class, args);
    }
}
