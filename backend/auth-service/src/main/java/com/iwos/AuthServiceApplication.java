package com.iwos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Authentication and Authorization Service
 *
 * This service handles:
 * - User registration and login
 * - JWT token generation and validation
 * - Role-based access control (RBAC)
 * - Refresh token management
 *
 * @author IWOS Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("""

            ╔══════════════════════════════════════════════╗
            ║   🔐 IWOS Auth Service Started Successfully   ║
            ║                                              ║
            ║   Port: 8081                                 ║
            ║   API Docs: http://localhost:8081/api/v1/swagger-ui.html
            ║   Health: http://localhost:8081/api/v1/actuator/health
            ╚══════════════════════════════════════════════╝
            """);
    }
}
