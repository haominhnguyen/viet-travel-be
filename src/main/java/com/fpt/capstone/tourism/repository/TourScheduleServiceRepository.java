package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface TourScheduleServiceRepository  extends JpaRepository<TourBookingService, Long>, JpaSpecificationExecutor<TourBookingService> {

    List<TourBookingService> findAllByBookingIn(List<TourBooking> bookings);
}
