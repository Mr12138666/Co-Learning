package com.colearning.study.dto.response;

import java.time.LocalDate;
import java.time.Instant;

public record CheckinResponse(
        Long id,
        LocalDate checkinDate,
        String planText,
        String reflectionText,
        Short mood,
        int focusTotalSec,
        boolean completed,
        String images,
        Instant createdAt,
        Instant updatedAt
) {
}
