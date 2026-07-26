package com.colearning.study.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkPlannedDateRequest(
        @NotNull List<Long> taskIds,
        LocalDate plannedDate
) {
}
