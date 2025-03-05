package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.AgeType;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourBookingCustomerRepository extends JpaRepository<TourBookingCustomer, Long> {
    List<TourBookingCustomer> findAllByTourBookingAndAgeTypeAndDeletedAndBookedPerson(TourBooking tourBooking, AgeType ageType, boolean deleted, boolean bookedPerson);
    TourBookingCustomer findByTourBookingAndBookedPerson(TourBooking tourBooking, boolean bookedPerson);
}
