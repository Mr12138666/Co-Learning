package com.colearning.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 7) String color
) {
}
