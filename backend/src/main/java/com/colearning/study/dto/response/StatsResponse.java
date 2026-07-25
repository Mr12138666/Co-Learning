package com.colearning.study.dto.response;

import java.time.LocalDate;
import java.util.List;

public record StatsResponse(
        int todayFocusSeconds,
        int weekFocusSeconds,
        int totalFocusSeconds,
        int streakDays,
        int weekCheckinCount,
        int weekCompletedCount,
        LocalDate lastCheckinDate,
        List<DailyStat> dailyStats,
        List<SubjectStat> subjectStats
) {
    public record DailyStat(
            LocalDate date,
            int focusSeconds,
            int sessionCount,
            boolean checkedIn
    ) {
    }

    public record SubjectStat(
            Long subjectId,
            String subjectName,
            String subjectColor,
            int focusSeconds,
            int sessionCount
    ) {
    }
}
