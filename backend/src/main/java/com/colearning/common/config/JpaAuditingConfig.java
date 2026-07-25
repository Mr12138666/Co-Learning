package com.colearning.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for @CreatedDate and @LastModifiedDate on entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
