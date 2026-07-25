package com.colearning.room.internal.repository;

import com.colearning.room.internal.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    /**
     * Find active members of a room (leftAt is null).
     */
    @Query("SELECT m FROM RoomMember m WHERE m.roomId = :roomId AND m.leftAt IS NULL ORDER BY m.joinedAt ASC")
    List<RoomMember> findActiveMembers(@Param("roomId") Long roomId);

    /**
     * Find a specific member by room and user (any state).
     */
    Optional<RoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * Find active member by room and user.
     */
    @Query("SELECT m FROM RoomMember m WHERE m.roomId = :roomId AND m.userId = :userId AND m.leftAt IS NULL")
    Optional<RoomMember> findActiveMember(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Count active members in a room.
     */
    @Query("SELECT COUNT(m) FROM RoomMember m WHERE m.roomId = :roomId AND m.leftAt IS NULL")
    long countActiveMembers(@Param("roomId") Long roomId);

    /**
     * Find all rooms a user is currently a member of.
     */
    @Query("SELECT m FROM RoomMember m WHERE m.userId = :userId AND m.leftAt IS NULL ORDER BY m.joinedAt DESC")
    List<RoomMember> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Mark a member as left.
     */
    @Modifying
    @Query("UPDATE RoomMember m SET m.leftAt = :leftAt WHERE m.id = :memberId AND m.leftAt IS NULL")
    int markLeft(@Param("memberId") Long memberId, @Param("leftAt") Instant leftAt);

    /**
     * Update mute status for a member.
     */
    @Modifying
    @Query("UPDATE RoomMember m SET m.mutedUntil = :mutedUntil WHERE m.id = :memberId")
    int updateMuteStatus(@Param("memberId") Long memberId, @Param("mutedUntil") Instant mutedUntil);
}
