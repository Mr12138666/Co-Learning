package com.colearning.room.internal.repository;

import com.colearning.room.internal.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find active, non-deleted public rooms ordered by creation date.
     */
    @Query("SELECT r FROM Room r WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' AND r.visibility = 'PUBLIC' ORDER BY r.createdAt DESC")
    Page<Room> findPublicRooms(Pageable pageable);

    /**
     * Find rooms owned by a user (including deleted for management).
     */
    @Query("SELECT r FROM Room r WHERE r.ownerId = :userId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Room> findByOwnerId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Check if a room name already exists.
     */
    boolean existsByName(String name);
}
