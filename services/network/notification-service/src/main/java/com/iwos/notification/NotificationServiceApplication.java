package com.iwos.notification;

import com.iwos.notification.infrastructure.config.NotificationServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NotificationServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Notification Service API",
        version = "v1",
        description = "Customer and operational notification read APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
