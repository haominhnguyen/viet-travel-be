package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {
}
