package com.colearning.gamification.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shop catalog item for pets.
 */
@Entity
@Table(name = "pet_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;  // FOOD | TOY | ACCESSORY

    @Column(name = "effect_type", nullable = false, length = 30)
    private String effectType;  // MOOD_BOOST | HUNGER_RESTORE | EXP_BOOST

    @Column(name = "effect_value", nullable = false)
    private Integer effectValue;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 50)
    private String icon;
}
