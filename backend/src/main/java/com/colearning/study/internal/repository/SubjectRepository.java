package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByUserIdOrderBySortOrderAsc(Long userId);

    Optional<Subject> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);
}
