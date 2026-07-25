package com.colearning.leaderboard.internal;

import com.colearning.leaderboard.LeaderboardService;
import com.colearning.leaderboard.dto.response.LeaderboardEntryResponse;
import com.colearning.leaderboard.dto.response.LeaderboardResponse;
import com.colearning.user.internal.entity.UserProfile;
import com.colearning.user.internal.repository.UserProfileRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

/**
 * Redis ZSET-based leaderboard implementation.
 *
 * <p>Keys:
 * <ul>
 *   <li>{@code lb:daily:{yyyy-MM-dd}} — daily leaderboard</li>
 *   <li>{@code lb:weekly:{yyyy-'W'ww}} — weekly leaderboard</li>
 *   <li>{@code lb:alltime} — all-time leaderboard</li>
 * </ul>
 *
 * <p>Score = focus seconds + check-in bonus (50) + streak bonus (10 * streakDays).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final String KEY_ALLTIME = "lb:alltime";
    private static final String KEY_DAILY_PREFIX = "lb:daily:";
    private static final String KEY_WEEKLY_PREFIX = "lb:weekly:";
    private static final DateTimeFormatter DAILY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;

    private final StringRedisTemplate redisTemplate;
    private final UserProfileRepository userProfileRepository;

    @Override
    public void addScore(Long userId, double score) {
        if (score <= 0) return;

        String userIdStr = String.valueOf(userId);
        String dailyKey = getDailyKey();
        String weeklyKey = getWeeklyKey();

        redisTemplate.opsForZSet().incrementScore(dailyKey, userIdStr, score);
        redisTemplate.opsForZSet().incrementScore(weeklyKey, userIdStr, score);
        redisTemplate.opsForZSet().incrementScore(KEY_ALLTIME, userIdStr, score);

        // Set TTL on daily/weekly keys (expire after 7/30 days)
        redisTemplate.expire(dailyKey, java.time.Duration.ofDays(7));
        redisTemplate.expire(weeklyKey, java.time.Duration.ofDays(30));

        log.debug("Added score {} to userId={} on all leaderboards", score, userId);
    }

    @Override
    public LeaderboardResponse getLeaderboard(String type, int limit, Long currentUserId) {
        String key = resolveKey(type);
        if (key == null) {
            return new LeaderboardResponse(type, List.of(), null);
        }

        // Fetch top N entries from Redis ZSET (highest score first)
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, limit - 1);

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        if (tuples != null && !tuples.isEmpty()) {
            // Batch-fetch user profiles
            Set<Long> userIds = new HashSet<>();
            for (TypedTuple<String> t : tuples) {
                if (t.getValue() != null) {
                    userIds.add(Long.parseLong(t.getValue()));
                }
            }
            Map<Long, UserProfile> profileMap = batchFetchProfiles(userIds);

            int rank = 1;
            for (TypedTuple<String> t : tuples) {
                if (t.getValue() == null) continue;
                Long uid = Long.parseLong(t.getValue());
                UserProfile profile = profileMap.get(uid);
                entries.add(new LeaderboardEntryResponse(
                        uid,
                        profile != null ? profile.getDisplayName() : "User " + uid,
                        profile != null ? profile.getAvatarUrl() : null,
                        rank,
                        t.getScore() != null ? t.getScore() : 0.0
                ));
                rank++;
            }
        }

        // Get current user's rank
        LeaderboardEntryResponse myRank = buildUserRank(key, currentUserId);

        return new LeaderboardResponse(type, entries, myRank);
    }

    @Override
    public LeaderboardResponse getMyRank(String type, Long userId) {
        String key = resolveKey(type);
        if (key == null) {
            return new LeaderboardResponse(type, List.of(), null);
        }
        return new LeaderboardResponse(type, List.of(), buildUserRank(key, userId));
    }

    // ===== Helper methods =====

    private String resolveKey(String type) {
        return switch (type) {
            case "daily" -> getDailyKey();
            case "weekly" -> getWeeklyKey();
            case "alltime" -> KEY_ALLTIME;
            default -> null;
        };
    }

    private String getDailyKey() {
        return KEY_DAILY_PREFIX + LocalDate.now().format(DAILY_FMT);
    }

    private String getWeeklyKey() {
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(WEEK_FIELDS.weekOfWeekBasedYear());
        int year = now.get(WEEK_FIELDS.weekBasedYear());
        return KEY_WEEKLY_PREFIX + year + "-W" + String.format("%02d", weekNumber);
    }

    private LeaderboardEntryResponse buildUserRank(String key, Long userId) {
        if (userId == null) return null;

        String userIdStr = String.valueOf(userId);
        Double score = redisTemplate.opsForZSet().score(key, userIdStr);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userIdStr);

        if (score == null) {
            // User not on leaderboard yet
            UserProfile profile = userProfileRepository.findById(userId).orElse(null);
            return new LeaderboardEntryResponse(
                    userId,
                    profile != null ? profile.getDisplayName() : "User " + userId,
                    profile != null ? profile.getAvatarUrl() : null,
                    -1,
                    0.0
            );
        }

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        return new LeaderboardEntryResponse(
                userId,
                profile != null ? profile.getDisplayName() : "User " + userId,
                profile != null ? profile.getAvatarUrl() : null,
                rank != null ? rank.intValue() + 1 : -1,  // ZSET rank is 0-indexed
                score
        );
    }

    private Map<Long, UserProfile> batchFetchProfiles(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userProfileRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p));
    }
}
