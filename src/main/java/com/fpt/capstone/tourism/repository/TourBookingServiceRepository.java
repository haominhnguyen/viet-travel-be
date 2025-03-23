package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import org.apache.catalina.LifecycleState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TourBookingServiceRepository extends JpaRepository<TourBookingService, Long>, JpaSpecificationExecutor<TourBookingService> {

    TourBookingService findByBookingIdAndServiceIdAndDeletedFalse(Long bookingId, Long serviceId);

    Page<TourBookingService> findByStatusIn(List<TourBookingServiceStatus> statuses, Pageable pageable);


}
