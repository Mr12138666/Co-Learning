package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.PetItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetItemRepository extends JpaRepository<PetItem, Long> {
}
