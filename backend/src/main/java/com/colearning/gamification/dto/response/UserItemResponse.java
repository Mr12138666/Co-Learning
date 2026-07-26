package com.colearning.gamification.dto.response;

import com.colearning.gamification.internal.entity.PetItem;
import com.colearning.gamification.internal.entity.UserItem;

/**
 * Response for a user-owned item (inventory entry).
 */
public record UserItemResponse(
        Long id,
        Long itemId,
        String name,
        String description,
        String itemType,
        String effectType,
        int effectValue,
        int quantity,
        String icon
) {
    public static UserItemResponse from(UserItem userItem, PetItem petItem) {
        return new UserItemResponse(
                userItem.getId(),
                petItem.getId(),
                petItem.getName(),
                petItem.getDescription(),
                petItem.getItemType(),
                petItem.getEffectType(),
                petItem.getEffectValue(),
                userItem.getQuantity(),
                petItem.getIcon()
        );
    }
}
