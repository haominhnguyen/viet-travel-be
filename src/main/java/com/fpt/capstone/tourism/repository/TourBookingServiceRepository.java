package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBookingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TourBookingServiceRepository extends JpaRepository<TourBookingService, Long>, JpaSpecificationExecutor<TourBookingService> {

    TourBookingService findByBookingIdAndServiceIdAndDeletedFalse(Long bookingId, Long serviceId);
}
