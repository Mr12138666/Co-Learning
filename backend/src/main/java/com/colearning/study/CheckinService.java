package com.colearning.study;

import com.colearning.study.dto.request.UpdateCheckinRequest;
import com.colearning.study.dto.response.CheckinResponse;

/**
 * Service for daily check-ins and reflections.
 */
public interface CheckinService {

    /**
     * Get or create today's check-in record for the user.
     */
    CheckinResponse getOrCreateToday(Long userId);

    /**
     * Update check-in content (plan, reflection, mood).
     */
    CheckinResponse updateCheckin(Long userId, UpdateCheckinRequest request);

    /**
     * Mark today's check-in as completed. Publishes DailyCheckinCompletedEvent.
     */
    CheckinResponse completeCheckin(Long userId);

    /**
     * Get check-in for a specific date.
     */
    CheckinResponse getCheckinByDate(Long userId, java.time.LocalDate date);
}
