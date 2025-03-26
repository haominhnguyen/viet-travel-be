package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.model.Activity;
import com.fpt.capstone.tourism.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {
    @Query(value = "SELECT * FROM activity WHERE is_deleted = FALSE ORDER BY RANDOM() LIMIT :numberActivity", nativeQuery = true)
    List<Activity> findRandomActivities( int numberActivity);

    Activity findByTitle(String title);
    @Query(value = "SELECT * FROM activity WHERE is_deleted = FALSE AND location_id =:locationId ORDER BY RANDOM() LIMIT :numberActivity", nativeQuery = true)
    List<Activity> findRelatedActivities(@Param("locationId") Long locationId, int numberActivity);

    List<Activity> findByDeletedFalse();
    List<Activity> findByActivityCategoryIdAndDeletedFalse(Long categoryId);
    List<Activity> findByLocationIdAndDeletedFalse(Long locationId);

    List<Activity> findByLocationIdAndActivityCategoryIdAndDeletedFalse(Long locationId, Long categoryId);
}

