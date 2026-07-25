package com.colearning.room.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @Min(2) @Max(100) Integer maxMembers,
    String visibility,  // PUBLIC | PRIVATE
    String password,    // required if visibility=PRIVATE
    @Size(max = 200) String topic
) {}
