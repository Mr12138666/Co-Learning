package com.colearning.user.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateSettingsRequest(
        @Pattern(regexp = "PUBLIC|FRIENDS|PRIVATE") String privacyLevel,
        Boolean notifEmailEnabled,
        Boolean notifPushEnabled,
        String timezone
) {}
