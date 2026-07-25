package com.colearning.common.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a injectable {@link Clock} for testable time-dependent code.
 * In production uses system UTC clock; in tests can be overridden.
 */
@Configuration
public class ClockProvider {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    public static Instant now(Clock clock) {
        return Instant.now(clock);
    }

    public static ZoneId defaultZone() {
        return ZoneId.of("Asia/Shanghai");
    }
}
