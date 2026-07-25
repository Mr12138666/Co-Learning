package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.UserItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);
}
