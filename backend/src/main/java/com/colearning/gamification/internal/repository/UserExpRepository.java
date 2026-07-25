package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.UserExp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExpRepository extends JpaRepository<UserExp, Long> {

    Optional<UserExp> findByUserId(Long userId);
}
