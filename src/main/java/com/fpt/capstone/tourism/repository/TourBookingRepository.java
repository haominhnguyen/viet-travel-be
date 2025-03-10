package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long>, JpaSpecificationExecutor<TourBooking> {
    TourBooking findByBookingCode(String bookingCode);

    List<TourBooking> findByTourSchedule_Id(Long scheduleId);

}
