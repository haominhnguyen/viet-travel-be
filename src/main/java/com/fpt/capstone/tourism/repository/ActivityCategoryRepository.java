package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {
    List<ActivityCategory> findByDeletedFalse();
}
