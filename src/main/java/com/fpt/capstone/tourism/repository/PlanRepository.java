package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

}
