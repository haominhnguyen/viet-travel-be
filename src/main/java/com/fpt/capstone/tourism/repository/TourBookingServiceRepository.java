package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.TourDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TourBookingServiceRepository extends JpaRepository<TourBookingService, Long>, JpaSpecificationExecutor<TourBookingService> {

    TourBookingService findByBookingIdAndServiceIdAndDeletedFalse(Long bookingId, Long serviceId);

    List<TourBookingService> findByTourDayAndBooking(TourDay tourDay, TourBooking tourBooking);
}
