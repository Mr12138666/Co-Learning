package com.colearning.room.dto.request;

public record JoinRoomRequest(
    String password  // null for public rooms
) {}
