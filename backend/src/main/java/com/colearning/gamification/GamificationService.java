package com.colearning.gamification;

import com.colearning.gamification.dto.request.RenamePetRequest;
import com.colearning.gamification.dto.response.AchievementResponse;
import com.colearning.gamification.dto.response.GamificationProfileResponse;
import com.colearning.gamification.dto.response.PetItemResponse;
import com.colearning.gamification.dto.response.PetResponse;
import com.colearning.gamification.dto.response.UserItemResponse;
import java.util.List;

/**
 * Service for gamification: experience, tokens, pets, and achievements.
 */
public interface GamificationService {

    // ===== Experience & Tokens =====

    GamificationProfileResponse getProfile(Long userId);

    void addExp(Long userId, int exp);

    void addTokens(Long userId, int tokens);

    // ===== Pet =====

    PetResponse getPet(Long userId);

    PetResponse renamePet(Long userId, RenamePetRequest request);

    PetResponse feedPet(Long userId, Long itemId);

    PetResponse interactPet(Long userId, Long itemId);

    // ===== Shop & Items =====

    List<PetItemResponse> getShopItems();

    List<UserItemResponse> getInventory(Long userId);

    void purchaseItem(Long userId, Long itemId);

    // ===== Achievements =====

    List<AchievementResponse> getAchievements(Long userId);

    void checkAndUnlockAchievements(Long userId);
}
