package com.colearning.journal.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateJournalRequest(
        @Size(max = 200) String title,
        @Size(max = 50000) String contentMarkdown,
        String visibility,
        String status,  // DRAFT | PUBLISHED
        Long roomId
) {
}
