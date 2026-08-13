package com.colearning.common.dto;

import java.time.Instant;
import lombok.Data;

/**
 * Base DTO class for all responses.
 * Contains common fields for all DTOs.
 */
@Data
public abstract class BaseDto {
    
    /**
     * Entity ID.
     */
    private Long id;
    
    /**
     * Creation timestamp.
     */
    private Instant createdAt;
    
    /**
     * Last update timestamp.
     */
    private InstantUpdatedAt;
    
    /**
     * Convert entity to DTO.
     *
     * @param <E> Entity type
     * @param <D> DTO type
     */
    public interface Converter<E, D> {
        
        /**
         * Convert entity to DTO.
         *
         * @param entity The entity
         * @return The DTO
         */
        D toDto(E entity);
    }
}