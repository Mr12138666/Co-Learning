package com.colearning.study;

import com.colearning.study.dto.request.UpdateCheckinRequest;
import com.colearning.study.dto.response.CheckinResponse;
import java.time.LocalDate;
import java.util.List;

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
    CheckinResponse getCheckinByDate(Long userId, LocalDate date);

    /**
     * Get check-in history within a date range.
     */
    List<CheckinResponse> getHistory(Long userId, LocalDate from, LocalDate to);
}
