package com.iwos.taskexecution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class TaskExecutionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskExecutionServiceApplication.class, args);
    }
}
