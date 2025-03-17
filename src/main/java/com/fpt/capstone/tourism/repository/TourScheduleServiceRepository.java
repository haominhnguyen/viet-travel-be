package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.TourScheduleService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface TourScheduleServiceRepository  extends JpaRepository<TourScheduleService, Long>, JpaSpecificationExecutor<TourScheduleService> {
    List<TourScheduleService> findByTourSchedule_Id(Long scheduleId);
}
