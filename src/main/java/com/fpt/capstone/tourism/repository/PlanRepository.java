package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Plan;
import com.fpt.capstone.tourism.model.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {
    @Query("select p from Plan p where p.user.id = :userId")
    List<Plan> getByUserId(@Param("userId") Long userId);
}
