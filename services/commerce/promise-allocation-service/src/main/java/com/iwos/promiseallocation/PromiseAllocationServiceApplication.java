package com.iwos.promiseallocation;

import com.iwos.promiseallocation.infrastructure.config.PromiseAllocationServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PromiseAllocationServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Promise Allocation Service API",
        version = "v1",
        description = "Fulfillment promise evaluation and node allocation APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class PromiseAllocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromiseAllocationServiceApplication.class, args);
    }
}
