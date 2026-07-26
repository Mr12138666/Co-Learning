package com.colearning.study.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCheckinRequest(
        LocalDate checkinDate,
        @Size(max = 1000) String planText,
        @Size(max = 2000) String reflectionText,
        @Min(1) @Max(5) Short mood,
        String images
) {
}
