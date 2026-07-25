package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.DailyPlan;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {

    Optional<DailyPlan> findByUserIdAndPlanDate(Long userId, LocalDate planDate);
}
