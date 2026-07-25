package com.colearning.gamification.internal;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.gamification.GamificationService;
import com.colearning.gamification.dto.request.RenamePetRequest;
import com.colearning.gamification.dto.response.AchievementResponse;
import com.colearning.gamification.dto.response.GamificationProfileResponse;
import com.colearning.gamification.dto.response.PetItemResponse;
import com.colearning.gamification.dto.response.PetResponse;
import com.colearning.gamification.internal.entity.Achievement;
import com.colearning.gamification.internal.entity.Pet;
import com.colearning.gamification.internal.entity.PetItem;
import com.colearning.gamification.internal.entity.UserAchievement;
import com.colearning.gamification.internal.entity.UserExp;
import com.colearning.gamification.internal.entity.UserItem;
import com.colearning.gamification.internal.repository.AchievementRepository;
import com.colearning.gamification.internal.repository.PetItemRepository;
import com.colearning.gamification.internal.repository.PetRepository;
import com.colearning.gamification.internal.repository.UserAchievementRepository;
import com.colearning.gamification.internal.repository.UserExpRepository;
import com.colearning.gamification.internal.repository.UserItemRepository;
import com.colearning.study.StatsService;
import com.colearning.study.dto.response.StatsResponse;
import com.colearning.study.internal.repository.DailyCheckinRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link GamificationService}.
 * Manages experience, tokens, pets, shop, and achievements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationServiceImpl implements GamificationService {

    private final UserExpRepository userExpRepository;
    private final PetRepository petRepository;
    private final PetItemRepository petItemRepository;
    private final UserItemRepository userItemRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final StatsService statsService;
    private final DailyCheckinRepository dailyCheckinRepository;

    // ===== Experience & Tokens =====

    @Override
    @Transactional
    public GamificationProfileResponse getProfile(Long userId) {
        UserExp exp = getOrCreateUserExp(userId);
        return GamificationProfileResponse.from(userId, exp.getTotalExp(), exp.getTokens());
    }

    @Override
    @Transactional
    public void addExp(Long userId, int exp) {
        if (exp <= 0) return;
        UserExp userExp = getOrCreateUserExp(userId);
        userExp.setTotalExp(userExp.getTotalExp() + exp);
        int newLevel = GamificationProfileResponse.calculateLevel(userExp.getTotalExp());
        if (newLevel > userExp.getLevel()) {
            log.info("User {} leveled up: {} -> {}", userId, userExp.getLevel(), newLevel);
            userExp.setLevel(newLevel);
        }
        userExpRepository.save(userExp);
    }

    @Override
    @Transactional
    public void addTokens(Long userId, int tokens) {
        if (tokens <= 0) return;
        UserExp userExp = getOrCreateUserExp(userId);
        userExp.setTokens(userExp.getTokens() + tokens);
        userExpRepository.save(userExp);
    }

    // ===== Pet =====

    @Override
    @Transactional
    public PetResponse getPet(Long userId) {
        Pet pet = getOrCreatePet(userId);
        // Decay mood/hunger based on time since last interaction
        decayPetStats(pet);
        return PetResponse.from(petRepository.save(pet));
    }

    @Override
    @Transactional
    public PetResponse renamePet(Long userId, RenamePetRequest request) {
        Pet pet = getOrCreatePet(userId);
        pet.setName(request.name());
        return PetResponse.from(petRepository.save(pet));
    }

    @Override
    @Transactional
    public PetResponse feedPet(Long userId, Long itemId) {
        Pet pet = getOrCreatePet(userId);
        UserItem userItem = userItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));
        PetItem item = petItemRepository.findById(itemId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));

        // Apply effect
        if ("HUNGER_RESTORE".equals(item.getEffectType())) {
            pet.setHunger(Math.min(100, pet.getHunger() + item.getEffectValue()));
        } else if ("MOOD_BOOST".equals(item.getEffectType())) {
            pet.setMood(Math.min(100, pet.getMood() + item.getEffectValue()));
        } else if ("EXP_BOOST".equals(item.getEffectType())) {
            pet.setExp(pet.getExp() + item.getEffectValue());
        }
        pet.setLastFedAt(Instant.now());

        // Consume item
        consumeItem(userItem);

        return PetResponse.from(petRepository.save(pet));
    }

    @Override
    @Transactional
    public PetResponse interactPet(Long userId, Long itemId) {
        Pet pet = getOrCreatePet(userId);
        UserItem userItem = userItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));
        PetItem item = petItemRepository.findById(itemId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));

        // Toys boost mood
        if ("MOOD_BOOST".equals(item.getEffectType())) {
            pet.setMood(Math.min(100, pet.getMood() + item.getEffectValue()));
        }
        pet.setLastInteractedAt(Instant.now());

        consumeItem(userItem);

        return PetResponse.from(petRepository.save(pet));
    }

    // ===== Shop & Items =====

    @Override
    public List<PetItemResponse> getShopItems() {
        return petItemRepository.findAll().stream()
                .map(PetItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void purchaseItem(Long userId, Long itemId) {
        PetItem item = petItemRepository.findById(itemId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));
        UserExp userExp = getOrCreateUserExp(userId);

        if (userExp.getTokens() < item.getPrice()) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_TOKENS);
        }

        // Deduct tokens
        userExp.setTokens(userExp.getTokens() - item.getPrice());
        userExpRepository.save(userExp);

        // Add or update user item
        UserItem userItem = userItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseGet(() -> UserItem.builder()
                        .userId(userId)
                        .itemId(itemId)
                        .quantity(0)
                        .build());
        userItem.setQuantity(userItem.getQuantity() + 1);
        userItemRepository.save(userItem);
    }

    // ===== Achievements =====

    @Override
    public List<AchievementResponse> getAchievements(Long userId) {
        List<Achievement> all = achievementRepository.findAll();
        List<UserAchievement> unlocked = userAchievementRepository.findByUserId(userId);
        Map<Long, Instant> unlockedMap = unlocked.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, UserAchievement::getUnlockedAt));

        return all.stream()
                .map(ach -> AchievementResponse.from(
                        ach,
                        unlockedMap.containsKey(ach.getId()),
                        unlockedMap.get(ach.getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public void checkAndUnlockAchievements(Long userId) {
        List<Achievement> all = achievementRepository.findAll();
        Set<Long> alreadyUnlocked = userAchievementRepository.findByUserId(userId).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        // Gather user stats for condition checking
        StatsResponse stats = statsService.getStats(userId);
        long totalCheckins = dailyCheckinRepository
                .findByUserIdAndCompletedTrueOrderByCheckinDateDesc(userId).size();
        UserExp userExp = getOrCreateUserExp(userId);

        for (Achievement ach : all) {
            if (alreadyUnlocked.contains(ach.getId())) continue;

            boolean met = switch (ach.getConditionType()) {
                case "FOCUS_TOTAL_SEC" -> stats.totalFocusSeconds() >= ach.getConditionValue();
                case "STREAK_DAYS" -> stats.streakDays() >= ach.getConditionValue();
                case "CHECKIN_COUNT" -> totalCheckins >= ach.getConditionValue();
                case "LEVEL" -> userExp.getLevel() >= ach.getConditionValue();
                default -> false;
            };

            if (met) {
                unlockAchievement(userId, ach);
            }
        }
    }

    // ===== Private helpers =====

    private UserExp getOrCreateUserExp(Long userId) {
        return userExpRepository.findByUserId(userId)
                .orElseGet(() -> userExpRepository.save(
                        UserExp.builder().userId(userId).build()));
    }

    private Pet getOrCreatePet(Long userId) {
        return petRepository.findByUserId(userId)
                .orElseGet(() -> petRepository.save(
                        Pet.builder().userId(userId).name("小伴").build()));
    }

    private void consumeItem(UserItem userItem) {
        userItem.setQuantity(userItem.getQuantity() - 1);
        if (userItem.getQuantity() <= 0) {
            userItemRepository.delete(userItem);
        } else {
            userItemRepository.save(userItem);
        }
    }

    private void unlockAchievement(Long userId, Achievement ach) {
        UserAchievement ua = UserAchievement.builder()
                .userId(userId)
                .achievementId(ach.getId())
                .build();
        userAchievementRepository.save(ua);

        // Award rewards
        if (ach.getExpReward() > 0) {
            addExp(userId, ach.getExpReward());
        }
        if (ach.getTokenReward() > 0) {
            addTokens(userId, ach.getTokenReward());
        }

        log.info("User {} unlocked achievement: {} ({})", userId, ach.getCode(), ach.getName());
    }

    /**
     * Decay pet mood and hunger based on time since last fed/interacted.
     * Roughly: -1 per hour since last interaction, minimum 0.
     */
    private void decayPetStats(Pet pet) {
        Instant now = Instant.now();
        Instant lastAction = pet.getLastFedAt() != null ? pet.getLastFedAt() : pet.getCreatedAt();

        long hoursSince = java.time.Duration.between(lastAction, now).toHours();
        if (hoursSince > 0) {
            int decay = (int) Math.min(hoursSince, 100);
            pet.setHunger(Math.max(0, pet.getHunger() - decay));
            pet.setMood(Math.max(0, pet.getMood() - decay / 2));
        }
    }
}
