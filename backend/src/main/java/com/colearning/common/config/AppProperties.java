package com.colearning.common.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration properties bound from application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl;
    private String apiPrefix;
    private Cors cors = new Cors();
    private Jwt jwt = new Jwt();
    private Storage storage = new Storage();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private String[] allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
        private long maxAge;
    }

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenTtl;
        private long refreshTokenTtl;
        private String refreshTokenPrefix;
    }

    @Data
    public static class Storage {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketAvatars;
        private String bucketPets;
    }

    @Data
    public static class RateLimit {
        private RateLimitConfig login = new RateLimitConfig();
        private RateLimitConfig email = new RateLimitConfig();
    }

    @Data
    public static class RateLimitConfig {
        private int maxAttempts;
        private int windowSeconds;
    }
}
