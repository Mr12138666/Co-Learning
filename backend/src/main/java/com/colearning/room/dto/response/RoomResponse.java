package com.colearning.room.dto.response;

import java.time.Instant;

public record RoomResponse(
    Long id,
    String name,
    String description,
    Integer maxMembers,
    String visibility,
    String status,
    String topic,
    Long ownerId,
    String ownerName,
    String ownerAvatar,
    Long memberCount,
    Boolean isMember,
    Instant createdAt,
    Instant updatedAt
) {}
