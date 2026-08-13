package com.colearning.common.repository;

import com.colearning.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for all entities.
 * Provides common query methods.
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity, ID> extends JpaRepository<E, ID> {
    
    /**
     * Find all entities by user ID.
     *
     * @param userId The user ID
     * @return List of entities
     */
    java.util.List<E> findByUserId(Long userId);
    
    /**
     * Count entities by user ID.
     *
     * @param userId The user ID
     * @return Count of entities
     */
    long countByUserId(Long userId);
    
    /**
     * Check if entity exists by user ID.
     *
     * @param userId The user ID
     * @return true if exists
     */
    boolean existsByUserId(Long userId);
}