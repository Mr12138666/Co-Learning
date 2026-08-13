package com.colearning.common.service;

import com.colearning.common.entity.BaseEntity;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base implementation of CrudService.
 * Provides common CRUD operations with transaction management.
 *
 * @param <E>  Entity type
 * @param <ID> Entity ID type
 * @param <C>  Create request type
 * @param <U>  Update request type
 * @param <R>  Response type
 */
@RequiredArgsConstructor
public abstract class BaseCrudService<E extends BaseEntity, ID, C, U, R> 
        implements CrudService<E, ID, C, U, R> {
    
    @PersistenceContext
    protected EntityManager entityManager;
    
    /**
     * Get the repository for this entity.
     *
     * @return The JPA repository
     */
    protected abstract JpaRepository<E, ID> getRepository();
    
    /**
     * Convert create request to entity.
     *
     * @param userId  The user ID
     * @param request The create request
     * @return The entity
     */
    protected abstract E toEntity(Long userId, C request);
    
    /**
     * Convert entity to response.
     *
     * @param entity The entity
     * @return The response
     */
    protected abstract R toResponse(E entity);
    
    /**
     * Update entity from request.
     *
     * @param entity  The entity to update
     * @param request The update request
     */
    protected abstract void updateEntity(E entity, U request);
    
    /**
     * Validate entity ownership.
     *
     * @param entity The entity
     * @param userId The user ID
     * @throws BusinessException if entity doesn't belong to user
     */
    protected abstract void validateOwnership(E entity, Long userId);
    
    @Override
    @Transactional
    public R create(Long userId, C request) {
        E entity = toEntity(userId, request);
        entity = getRepository().save(entity);
        return toResponse(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<R> list(Long userId) {
        return getRepository().findAll().stream()
                .filter(entity -> {
                    try {
                        validateOwnership(entity, userId);
                        return true;
                    } catch (BusinessException e) {
                        return false;
                    }
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public R getById(Long userId, ID id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        validateOwnership(entity, userId);
        return toResponse(entity);
    }
    
    @Override
    @Transactional
    public R update(Long userId, ID id, U request) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        validateOwnership(entity, userId);
        updateEntity(entity, request);
        entity = getRepository().save(entity);
        return toResponse(entity);
    }
    
    @Override
    @Transactional
    public void delete(Long userId, ID id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        validateOwnership(entity, userId);
        getRepository().delete(entity);
    }
    
    /**
     * Helper method to find entity by ID or throw NOT_FOUND.
     *
     * @param id The entity ID
     * @return The entity
     */
    protected E findByIdOrThrow(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
    }
}