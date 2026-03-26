package com.iwos.catalog.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

    @Bean
    public NewTopic catalogEventsTopic() {
        return new NewTopic("catalog.events", 3, (short) 1);
    }
}
