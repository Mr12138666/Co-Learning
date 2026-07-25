package com.colearning.journal.dto.response;

import java.time.Instant;

public record JournalResponse(
        Long id,
        String title,
        String contentMarkdown,
        String contentHtml,
        String visibility,
        Long roomId,
        String status,
        Instant publishedAt,
        String aiSummary,
        Instant createdAt,
        Instant updatedAt
) {
}
