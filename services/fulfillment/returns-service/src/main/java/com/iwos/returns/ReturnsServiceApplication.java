package com.iwos.returns;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "IWOS Returns Service API",
        version = "v1",
        description = "Reverse logistics lifecycle and return request APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ReturnsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReturnsServiceApplication.class, args);
    }
}
