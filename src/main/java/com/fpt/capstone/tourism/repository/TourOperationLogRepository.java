package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourImage;
import com.fpt.capstone.tourism.model.TourOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourOperationLogRepository  extends JpaRepository<TourOperationLog, Long>, JpaSpecificationExecutor<TourOperationLog> {
    List<TourOperationLog> findByTourSchedule_IdAndDeletedFalse(Long scheduleId);
}
