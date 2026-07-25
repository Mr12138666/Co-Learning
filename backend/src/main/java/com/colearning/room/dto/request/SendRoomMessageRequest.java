package com.colearning.room.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendRoomMessageRequest(
    @NotBlank @Size(max = 500) String content,
    String messageType,   // TEXT | FOCUS_STATUS (default TEXT)
    String focusStatus    // STUDYING | PAUSED | IDLE (for FOCUS_STATUS type)
) {}
