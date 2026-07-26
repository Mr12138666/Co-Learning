package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);

    @Query(value = "SELECT t.* FROM tags t JOIN task_tags tt ON t.id = tt.tag_id WHERE tt.task_id = :taskId ORDER BY t.name ASC", nativeQuery = true)
    List<Tag> findByTaskId(@Param("taskId") Long taskId);

    // Batch load task-tag associations to avoid N+1 when assembling task lists
    @Query(value = "SELECT tt.task_id, tt.tag_id FROM task_tags tt WHERE tt.task_id IN (:taskIds)", nativeQuery = true)
    List<Object[]> findTaskTagPairsByTaskIds(@Param("taskIds") List<Long> taskIds);

    @Modifying
    @Query(value = "INSERT INTO task_tags (task_id, tag_id) VALUES (:taskId, :tagId)", nativeQuery = true)
    void insertTaskTag(@Param("taskId") Long taskId, @Param("tagId") Long tagId);

    @Modifying
    @Query(value = "DELETE FROM task_tags WHERE task_id = :taskId", nativeQuery = true)
    void deleteTaskTagsByTaskId(@Param("taskId") Long taskId);
}
