package com.iwos.testkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Shared test configuration for all services.
 * Provides beans commonly needed across test suites.
 */
@TestConfiguration
public class SharedTestConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }

    @Bean
    public ApiTestClient apiTestClient() {
        return new ApiTestClient();
    }
}
