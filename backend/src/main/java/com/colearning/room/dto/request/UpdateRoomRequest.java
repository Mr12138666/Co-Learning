package com.colearning.room.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateRoomRequest(
    @Size(max = 100) String name,
    @Size(max = 500) String description,
    Integer maxMembers,
    String status,      // ACTIVE | CLOSED
    String password,
    @Size(max = 200) String topic
) {}
