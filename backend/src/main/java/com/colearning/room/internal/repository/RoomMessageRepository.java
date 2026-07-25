package com.colearning.room.internal.repository;

import com.colearning.room.internal.entity.RoomMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {

    /**
     * Find messages in a room ordered by creation date descending (newest first).
     */
    @Query("SELECT m FROM RoomMessage m WHERE m.roomId = :roomId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<RoomMessage> findByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * Find recent messages for reconnect snapshot (limited, oldest to newest).
     */
    @Query(value = "SELECT * FROM room_messages WHERE room_id = :roomId AND deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<RoomMessage> findRecentMessages(@Param("roomId") Long roomId, @Param("limit") int limit);

    /**
     * Count messages in a room.
     */
    @Query("SELECT COUNT(m) FROM RoomMessage m WHERE m.roomId = :roomId AND m.deletedAt IS NULL")
    long countByRoomId(@Param("roomId") Long roomId);
}
