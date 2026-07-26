package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.DailyCheckin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, Long> {

    Optional<DailyCheckin> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    List<DailyCheckin> findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(
            Long userId, LocalDate start, LocalDate end);

    List<DailyCheckin> findByUserIdAndCompletedTrueOrderByCheckinDateDesc(Long userId);

    long countByUserIdAndCompletedTrueAndCheckinDateBetween(
            Long userId, LocalDate start, LocalDate end);

    long countByUserIdAndCompletedTrue(Long userId);
}
