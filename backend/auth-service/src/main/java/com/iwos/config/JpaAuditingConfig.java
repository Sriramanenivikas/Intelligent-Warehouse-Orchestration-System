package com.iwos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration
 * Enables automatic auditing of entities (CreatedDate, LastModifiedDate)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
