package com.colearning.common.service;

import com.colearning.common.entity.BaseEntity;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Helper class for common service operations.
 */
public final class ServiceHelper {
    
    private ServiceHelper() {
        // Utility class
    }
    
    /**
     * Find entity by ID or throw NOT_FOUND.
     *
     * @param repository The repository
     * @param id         The entity ID
     * @param <E>        Entity type
     * @param <ID>       Entity ID type
     * @return The entity
     */
    public static <E extends BaseEntity, ID> E findByIdOrThrow(
            JpaRepository<E, ID> repository, ID id) {
        return repository.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
    }
    
    /**
     * Find entity by ID and validate ownership.
     *
     * @param repository The repository
     * @param id         The entity ID
     * @param userId     The user ID
     * @param ownerGetter Function to get owner ID from entity
     * @param <E>        Entity type
     * @param <ID>       Entity ID type
     * @return The entity
     */
    public static <E extends BaseEntity, ID> E findByIdAndValidateOwnership(
            JpaRepository<E, ID> repository,
            ID id,
            Long userId,
            Function<E, Long> ownerGetter) {
        E entity = findByIdOrThrow(repository, id);
        Long ownerId = ownerGetter.apply(entity);
        if (!userId.equals(ownerId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN);
        }
        return entity;
    }
    
    /**
     * Convert list of entities to list of responses.
     *
     * @param entities The entities
     * @param converter The converter function
     * @param <E>       Entity type
     * @param <R>       Response type
     * @return List of responses
     */
    public static <E, R> List<R> toResponseList(List<E> entities, Function<E, R> converter) {
        return entities.stream()
                .map(converter)
                .collect(Collectors.toList());
    }
    
    /**
     * Filter entities by user ID.
     *
     * @param entities The entities
     * @param userId   The user ID
     * @param ownerGetter Function to get owner ID from entity
     * @param <E>       Entity type
     * @return Filtered list of entities
     */
    public static <E> List<E> filterByUserId(
            List<E> entities,
            Long userId,
            Function<E, Long> ownerGetter) {
        return entities.stream()
                .filter(entity -> userId.equals(ownerGetter.apply(entity)))
                .collect(Collectors.toList());
    }
}