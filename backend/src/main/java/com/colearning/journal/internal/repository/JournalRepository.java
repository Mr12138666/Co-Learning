package com.colearning.journal.internal.repository;

import com.colearning.journal.internal.entity.Journal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    List<Journal> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Journal> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Journal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    /**
     * Find published journals visible to the requester.
     * PUBLIC journals are visible to everyone.
     * FRIENDS visibility requires friendship (simplified: not implemented in v1).
     */
    @Query("SELECT j FROM Journal j WHERE j.status = 'PUBLISHED' " +
           "AND j.visibility = 'PUBLIC' " +
           "ORDER BY j.publishedAt DESC")
    Page<Journal> findPublicJournals(Pageable pageable);

    /**
     * Find published journals by a specific user that are visible to the requester.
     */
    @Query("SELECT j FROM Journal j WHERE j.userId = :userId " +
           "AND j.status = 'PUBLISHED' " +
           "AND (j.visibility = 'PUBLIC' OR j.userId = :viewerId) " +
           "ORDER BY j.publishedAt DESC")
    Page<Journal> findVisibleJournalsByUser(
            @Param("userId") Long userId,
            @Param("viewerId") Long viewerId,
            Pageable pageable);
}
