package com.iwos.noderegistry;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "IWOS Node Registry Service API",
        version = "v1",
        description = "Fulfillment node catalog and metadata query APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class NodeRegistryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeRegistryServiceApplication.class, args);
    }
}
