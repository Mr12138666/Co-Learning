package com.colearning.auth.internal;

import com.colearning.auth.internal.entity.User;
import com.colearning.auth.internal.repository.UserRepository;
import com.colearning.common.security.Argon2PasswordEncoder;
import com.colearning.user.internal.entity.UserProfile;
import com.colearning.user.internal.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds development test data on startup (dev profile only).
 * Creates: 1 admin + 2 test users with verified emails.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(1)
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@colearning.local", "admin123", "管理员", "ADMIN", true);
        seedUser("student@test.com", "student123", "备考学生", "USER", true);
        seedUser("newbie@test.com", "newbie123", "新同学", "USER", false);
        log.info("Dev data seeding completed");
    }

    private void seedUser(String email, String password, String displayName, String role, boolean emailVerified) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(emailVerified)
                .status("ACTIVE")
                .role(role)
                .build();
        user = userRepository.save(user);

        String defaultAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + email;
        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .displayName(displayName)
                .avatarUrl(defaultAvatar)
                .build();
        userProfileRepository.save(profile);

        log.info("Seeded user: {} ({})", email, role);
    }
}
