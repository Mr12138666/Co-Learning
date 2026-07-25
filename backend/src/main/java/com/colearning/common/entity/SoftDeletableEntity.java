package com.colearning.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Base entity for soft-deletable entities.
 * Adds a {@code deleted_at} column that is null for active records.
 * Hibernate's {@code @SQLRestriction} automatically filters out deleted records.
 */
@Getter
@Setter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}
