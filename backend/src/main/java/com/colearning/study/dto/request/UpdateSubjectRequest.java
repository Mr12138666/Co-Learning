package com.colearning.study.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateSubjectRequest(
        @Size(max = 50) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式须为 #RRGGBB") String color,
        Integer sortOrder
) {
}
