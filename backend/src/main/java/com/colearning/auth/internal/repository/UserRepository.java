package com.colearning.auth.internal.repository;

import com.colearning.auth.internal.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = u.failedLoginCount + 1, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void incrementFailedLogin(Long userId, Instant lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = 0, u.lockedUntil = NULL, u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void resetFailedLogin(Long userId, Instant loginTime);
}
