package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface MealRepository extends JpaRepository<Meal, Long> {
    Optional<Meal> findByServiceId(Long serviceId);
    Optional<Meal> findByServiceIdAndDeletedFalse(Long serviceId);
}
