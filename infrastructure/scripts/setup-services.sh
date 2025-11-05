#!/bin/bash

# Create application.yml for each service
create_application_yml() {
    local service=$1
    local port=$2
    
    mkdir -p "backend/${service}/src/main/resources"
    
    cat > "backend/${service}/src/main/resources/application.yml" << YAML
server:
  port: ${port}

spring:
  application:
    name: ${service}
  datasource:
    url: jdbc:postgresql://localhost:5432/iwos_db
    username: iwos_user
    password: iwos_password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
  kafka:
    bootstrap-servers: localhost:9093
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus

logging:
  level:
    root: INFO
    com.iwos: DEBUG
YAML

    echo "✅ Created ${service}/application.yml"
}

# Create main application class
create_main_class() {
    local service=$1
    local port=$2
    local class_name=$(echo $service | sed -e 's/-service//' | sed -e 's/-/ /g' | sed -e 's/\b\(.\)/\u\1/g' | sed -e 's/ //g')
    
    mkdir -p "backend/${service}/src/main/java/com/iwos"
    
    cat > "backend/${service}/src/main/java/com/iwos/${class_name}ServiceApplication.java" << JAVA
package com.iwos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ${class_name}ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(${class_name}ServiceApplication.class, args);
        System.out.println("\\n✅ ${class_name} Service started on port ${port}\\n");
    }
}
JAVA

    echo "✅ Created ${service}/${class_name}ServiceApplication.java"
}

# Generate for each service
create_application_yml "inventory-service" "8082"
create_main_class "inventory-service" "8082"

create_application_yml "order-service" "8083"
create_main_class "order-service" "8083"

create_application_yml "warehouse-service" "8084"
create_main_class "warehouse-service" "8084"

echo ""
echo "============================================"
echo "✅ All service configs created!"
echo "============================================"
