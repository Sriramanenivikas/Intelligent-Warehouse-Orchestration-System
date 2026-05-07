package com.iwos.payment;

import com.iwos.payment.infrastructure.config.PaymentServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = PaymentServiceProperties.class)
@EnableScheduling
@OpenAPIDefinition(info = @Info(
        title = "IWOS Payment Service API",
        version = "v1",
        description = "Payment authorization, capture simulation, and payment state APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
