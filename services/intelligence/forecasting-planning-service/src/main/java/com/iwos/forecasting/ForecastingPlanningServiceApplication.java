package com.iwos.forecasting;

import com.iwos.forecasting.infrastructure.config.ForecastingPlanningServiceProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ForecastingPlanningServiceProperties.class)
@OpenAPIDefinition(info = @Info(
        title = "IWOS Forecasting Planning Service API",
        version = "v1",
        description = "Demand forecasting, replenishment planning, and model run visibility APIs.",
        contact = @Contact(name = "IWOS Engineering", email = "engineering@iwos.local", url = "https://iwos.local")
))
public class ForecastingPlanningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForecastingPlanningServiceApplication.class, args);
    }
}
