package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.model.enums.AgeType;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourBookingCustomerRepository extends JpaRepository<TourBookingCustomer, Long> {
    List<TourBookingCustomer> findAllByTourBookingAndAgeTypeAndDeletedAndBookedPerson(TourBooking tourBooking, AgeType ageType, boolean deleted, boolean bookedPerson);
    TourBookingCustomer findByTourBookingAndBookedPerson(TourBooking tourBooking, boolean bookedPerson);

    List<TourBookingCustomer> findByTourBookingId(Long id);
    List<TourBookingCustomer> findByTourBooking_IdAndBookedPersonFalse(Long id);

    List<TourBookingCustomer> findByBookedPersonAndTourBooking(boolean booked, TourBooking tourBooking);

    @Query("""
    SELECT c FROM TourBookingCustomer c
    WHERE c.tourBooking.id = :bookingId AND c.bookedPerson = true
""")
    Optional<TourBookingCustomer> findBookedPerson(@Param("bookingId") Long bookingId);

}
