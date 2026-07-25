package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.Pet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Optional<Pet> findByUserId(Long userId);

    List<Pet> findByHungerLessThan(Integer threshold);
}
