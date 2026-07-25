package com.colearning.study.internal;

import com.colearning.study.StatsService;
import com.colearning.study.dto.response.StatsResponse;
import com.colearning.study.dto.response.StatsResponse.DailyStat;
import com.colearning.study.dto.response.StatsResponse.SubjectStat;
import com.colearning.study.internal.entity.DailyCheckin;
import com.colearning.study.internal.entity.FocusSession;
import com.colearning.study.internal.entity.Subject;
import com.colearning.study.internal.repository.DailyCheckinRepository;
import com.colearning.study.internal.repository.FocusSessionRepository;
import com.colearning.study.internal.repository.SubjectRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final FocusSessionRepository focusSessionRepository;
    private final DailyCheckinRepository dailyCheckinRepository;
    private final SubjectRepository subjectRepository;
    private final Clock clock;

    private static final int STREAK_MIN_MINUTES = 10;  // 10 min effective focus to count for streak
    private static final int DAILY_CAP_MINUTES = 240;   // 240 min daily cap for leaderboard

    @Override
    @Transactional(readOnly = true)
    public StatsResponse getStats(Long userId) {
        ZoneId zone = clock.getZone();
        LocalDate today = LocalDate.now(zone);
        LocalDate weekAgo = today.minusDays(6);

        Instant todayStart = today.atStartOfDay(zone).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();
        Instant weekStart = weekAgo.atStartOfDay(zone).toInstant();
        Instant weekEnd = todayEnd;

        // Today's focus
        int todayFocus = focusSessionRepository.sumEffectiveSecondsInRange(userId, todayStart, todayEnd);

        // Week's focus
        int weekFocus = focusSessionRepository.sumEffectiveSecondsInRange(userId, weekStart, weekEnd);

        // Total focus (all time)
        int totalFocus = focusSessionRepository.sumEffectiveSecondsInRange(
                userId, Instant.EPOCH, Instant.now(clock));

        // Streak
        int streak = calculateStreak(userId, zone, today);

        // Checkin counts
        long weekCheckinCount = dailyCheckinRepository.countByUserIdAndCompletedTrueAndCheckinDateBetween(
                userId, weekAgo, today);
        List<DailyCheckin> weekCheckins = dailyCheckinRepository
                .findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(userId, weekAgo, today);
        long weekCompletedCount = weekCheckins.stream()
                .filter(DailyCheckin::getCompleted)
                .count();

        LocalDate lastCheckinDate = weekCheckins.stream()
                .filter(DailyCheckin::getCompleted)
                .map(DailyCheckin::getCheckinDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Daily stats (last 7 days)
        List<DailyStat> dailyStats = buildDailyStats(userId, zone, weekAgo, today);

        // Subject stats (last 7 days)
        List<SubjectStat> subjectStats = buildSubjectStats(userId, weekStart, weekEnd);

        return new StatsResponse(
                todayFocus,
                weekFocus,
                totalFocus,
                streak,
                (int) weekCheckinCount,
                (int) weekCompletedCount,
                lastCheckinDate,
                dailyStats,
                subjectStats
        );
    }

    @Override
    @Transactional
    public void refreshTodayFocusTotal(Long userId) {
        ZoneId zone = clock.getZone();
        LocalDate today = LocalDate.now(zone);
        Instant todayStart = today.atStartOfDay(zone).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(zone).toInstant();

        int todayFocus = focusSessionRepository.sumEffectiveSecondsInRange(userId, todayStart, todayEnd);

        dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today)
                .ifPresent(checkin -> {
                    checkin.setFocusTotalSec(todayFocus);
                    log.debug("Updated checkin focus_total_sec: userId={}, date={}, sec={}",
                            userId, today, todayFocus);
                });
    }

    // ===== Private helpers =====

    private int calculateStreak(Long userId, ZoneId zone, LocalDate today) {
        int streak = 0;
        LocalDate date = today;

        // Look back up to 365 days
        for (int i = 0; i < 365; i++) {
            Instant dayStart = date.atStartOfDay(zone).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

            int dayFocus = focusSessionRepository.sumEffectiveSecondsInRange(userId, dayStart, dayEnd);

            // A day counts if focus >= 10 minutes
            if (dayFocus >= STREAK_MIN_MINUTES * 60) {
                streak++;
                date = date.minusDays(1);
            } else {
                // Allow today to be "in progress" without breaking streak
                if (i == 0 && dayFocus == 0) {
                    // Today has no focus yet, start from yesterday
                    date = date.minusDays(1);
                    continue;
                }
                break;
            }
        }

        return streak;
    }

    private List<DailyStat> buildDailyStats(Long userId, ZoneId zone,
                                             LocalDate startDate, LocalDate endDate) {
        // Get all finished sessions in range
        Instant rangeStart = startDate.atStartOfDay(zone).toInstant();
        Instant rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        List<FocusSession> sessions = focusSessionRepository
                .findFinishedSessionsInRange(userId, rangeStart, rangeEnd);

        // Get checkins in range
        List<DailyCheckin> checkins = dailyCheckinRepository
                .findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(userId, startDate, endDate);
        Map<LocalDate, Boolean> checkinMap = new HashMap<>();
        for (DailyCheckin c : checkins) {
            checkinMap.put(c.getCheckinDate(), c.getCompleted());
        }

        // Aggregate by date
        Map<LocalDate, int[]> focusByDate = new HashMap<>();  // [seconds, count]
        for (FocusSession s : sessions) {
            LocalDate sessionDate = s.getStartedAt().atZone(zone).toLocalDate();
            int[] agg = focusByDate.computeIfAbsent(sessionDate, k -> new int[2]);
            agg[0] += s.getEffectiveSeconds();
            agg[1]++;
        }

        List<DailyStat> result = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            int[] agg = focusByDate.getOrDefault(d, new int[2]);
            result.add(new DailyStat(d, agg[0], agg[1], checkinMap.getOrDefault(d, false)));
        }
        return result;
    }

    private List<SubjectStat> buildSubjectStats(Long userId, Instant rangeStart, Instant rangeEnd) {
        List<FocusSession> sessions = focusSessionRepository
                .findFinishedSessionsInRange(userId, rangeStart, rangeEnd);

        // Aggregate by subject
        Map<Long, int[]> bySubject = new HashMap<>();  // [seconds, count]
        for (FocusSession s : sessions) {
            if (s.getSubjectId() == null) continue;
            int[] agg = bySubject.computeIfAbsent(s.getSubjectId(), k -> new int[2]);
            agg[0] += s.getEffectiveSeconds();
            agg[1]++;
        }

        // Get subject names
        Map<Long, Subject> subjectMap = subjectRepository
                .findByUserIdOrderBySortOrderAsc(userId).stream()
                .collect(java.util.stream.Collectors.toMap(Subject::getId, s -> s));

        List<SubjectStat> result = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : bySubject.entrySet()) {
            Subject subject = subjectMap.get(entry.getKey());
            result.add(new SubjectStat(
                    entry.getKey(),
                    subject != null ? subject.getName() : "未知科目",
                    subject != null ? subject.getColor() : "#999999",
                    entry.getValue()[0],
                    entry.getValue()[1]
            ));
        }
        result.sort((a, b) -> Integer.compare(b.focusSeconds(), a.focusSeconds()));
        return result;
    }
}
