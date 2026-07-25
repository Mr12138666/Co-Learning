package com.colearning.gamification.dto.response;

import com.colearning.gamification.internal.entity.PetItem;

/**
 * Shop item response.
 */
public record PetItemResponse(
        Long id,
        String name,
        String description,
        String itemType,
        String effectType,
        int effectValue,
        int price,
        String icon
) {
    public static PetItemResponse from(PetItem item) {
        return new PetItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getItemType(),
                item.getEffectType(),
                item.getEffectValue(),
                item.getPrice(),
                item.getIcon()
        );
    }
}
