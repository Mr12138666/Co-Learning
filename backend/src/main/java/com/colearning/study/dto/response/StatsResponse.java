package com.colearning.study.dto.response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record StatsResponse(
        int todayFocusSeconds,
        int weekFocusSeconds,
        int monthFocusSeconds,
        int yearFocusSeconds,
        int totalFocusSeconds,
        int streakDays,
        int focusDays,
        int totalCheckins,
        int weekCheckinCount,
        int weekCompletedCount,
        LocalDate lastCheckinDate,
        List<DailyStat> dailyStats,
        List<WeeklyStat> weeklyStats,
        List<MonthlyStat> monthlyStats,
        List<SubjectStat> subjectStats
) {
    public record DailyStat(
            LocalDate date,
            int focusSeconds,
            int sessionCount,
            boolean checkedIn
    ) {
    }

    public record WeeklyStat(
            int weekOfYear,
            String weekLabel,
            int focusSeconds,
            int sessionCount,
            int checkinCount
    ) {
    }

    public record MonthlyStat(
            YearMonth month,
            String monthLabel,
            int focusSeconds,
            int sessionCount,
            int focusDays
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
