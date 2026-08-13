package com.colearning.common.service;

import java.util.List;

/**
 * Generic CRUD service interface.
 * Provides common CRUD operations for entities.
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 * @param <C>  Create request type
 * @param <U>  Update request type
 * @param <R>  Response type
 */
public interface CrudService<E, ID, C, U, R> {
    
    /**
     * Create a new entity.
     *
     * @param userId  The user ID
     * @param request The create request
     * @return The created entity response
     */
    R create(Long userId, C request);
    
    /**
     * List all entities for a user.
     *
     * @param userId The user ID
     * @return List of entity responses
     */
    List<R> list(Long userId);
    
    /**
     * Get an entity by ID.
     *
     * @param userId The user ID
     * @param id     The entity ID
     * @return The entity response
     */
    R getById(Long userId, ID id);
    
    /**
     * Update an entity.
     *
     * @param userId  The user ID
     * @param id      The entity ID
     * @param request The update request
     * @return The updated entity response
     */
    R update(Long userId, ID id, U request);
    
    /**
     * Delete an entity.
     *
     * @param userId The user ID
     * @param id     The entity ID
     */
    void delete(Long userId, ID id);
}