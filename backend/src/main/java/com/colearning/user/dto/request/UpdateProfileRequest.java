package com.colearning.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 50) String displayName,
        @Size(max = 500) String bio
) {}
