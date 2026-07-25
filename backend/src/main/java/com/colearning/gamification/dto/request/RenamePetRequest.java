package com.colearning.gamification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to rename a pet.
 */
public record RenamePetRequest(
        @NotBlank @Size(max = 50) String name
) {
}
