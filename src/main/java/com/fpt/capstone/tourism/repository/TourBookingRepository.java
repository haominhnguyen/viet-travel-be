package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long>, JpaSpecificationExecutor<TourBooking> {
    TourBooking findByBookingCode(String bookingCode);

    List<TourBooking> findByTourSchedule_Id(Long scheduleId);

    long countByTourAndStatusIn(Tour tour, List<TourBookingStatus> tourBookingStatuses);

}
