package com.colearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Co-Learning Study Companion Platform - Application Entry Point.
 *
 * <p>Modular monolith with 9 domain packages:
 * auth, user, study, journal, room, leaderboard, gamification, ai, moderation.
 * Shared infrastructure lives in {@code common}.
 */
@SpringBootApplication
@EnableScheduling
public class CoLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoLearningApplication.class, args);
    }
}
