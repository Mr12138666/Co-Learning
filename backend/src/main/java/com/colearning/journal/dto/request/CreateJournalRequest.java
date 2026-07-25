package com.colearning.journal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJournalRequest(
        @Size(max = 200) String title,
        @NotBlank @Size(max = 50000) String contentMarkdown,
        String visibility,  // PRIVATE | FRIENDS | ROOM | PUBLIC
        Long roomId
) {
}
