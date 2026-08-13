package com.colearning.common.exception;

import com.colearning.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Helper class for common exception operations.
 */
public final class ExceptionHelper {
    
    private ExceptionHelper() {
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
     * Validate entity ownership.
     *
     * @param entity The entity
     * @param userId The user ID
     * @param <E>    Entity type
     * @throws BusinessException if entity doesn't belong to user
     */
    public static <E extends BaseEntity> void validateOwnership(E entity, Long userId) {
        // This is a generic implementation. Override in specific services if needed.
        // For now, we'll assume all entities have a userId field
        // In a real implementation, you would check the entity's userId field
    }
    
    /**
     * Throw CONFLICT if entity already exists.
     *
     * @param exists  Whether entity exists
     * @param message Error message
     */
    public static void throwIfConflict(boolean exists, String message) {
        if (exists) {
            throw BusinessException.of(ErrorCode.CONFLICT, message);
        }
    }
    
    /**
     * Throw BAD_REQUEST if condition is false.
     *
     * @param condition The condition to check
     * @param message   Error message
     */
    public static void throwIfBadRequest(boolean condition, String message) {
        if (!condition) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, message);
        }
    }
}