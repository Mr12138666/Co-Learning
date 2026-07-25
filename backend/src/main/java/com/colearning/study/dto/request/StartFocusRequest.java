package com.colearning.study.dto.request;

import jakarta.validation.constraints.Size;

public record StartFocusRequest(
        Long subjectId,
        Long taskId,
        @Size(max = 64) String clientRequestId
) {
}
