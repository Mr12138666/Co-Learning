package com.colearning.gamification.dto.response;

import com.colearning.gamification.internal.entity.Pet;
import java.time.Instant;

/**
 * Pet information response.
 */
public record PetResponse(
        Long id,
        Long userId,
        String name,
        String species,
        int level,
        int exp,
        int mood,
        int hunger,
        Instant lastFedAt,
        Instant lastInteractedAt,
        Integer nextHungerDecayInMinutes,
        Integer nextMoodDecayInMinutes
) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getUserId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getLevel(),
                pet.getExp(),
                pet.getMood(),
                pet.getHunger(),
                pet.getLastFedAt(),
                pet.getLastInteractedAt(),
                null,
                null
        );
    }
    
    public static PetResponse from(Pet pet, Integer hungerDecayMinutes, Integer moodDecayMinutes) {
        return new PetResponse(
                pet.getId(),
                pet.getUserId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getLevel(),
                pet.getExp(),
                pet.getMood(),
                pet.getHunger(),
                pet.getLastFedAt(),
                pet.getLastInteractedAt(),
                hungerDecayMinutes,
                moodDecayMinutes
        );
    }
}
