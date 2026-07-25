package com.colearning;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.TestInstance;

/**
 * Abstract base class for integration tests.
 * Spins up PostgreSQL 16 and Redis 7 containers via Testcontainers.
 * Uses @ServiceConnection for auto-wiring Spring Boot's datasource and Redis config.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("colearning_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:7-alpine")
            .withReuse(true);
}
