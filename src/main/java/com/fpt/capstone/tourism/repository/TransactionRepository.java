package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByBookingAndCategory(TourBooking tourBooking, TransactionType transactionType);


    @Query("""
            SELECT t FROM Transaction t
                WHERE t.booking.tourSchedule.id = :scheduleId
            """)
    List<Transaction> findAllByTourScheduleId(@Param("scheduleId") Long scheduleId);

    List<Transaction> findByBooking_Id(Long bookingId);

    @Query("""
        SELECT COALESCE(SUM(ca.finalAmount), 0)
        FROM Transaction t
        JOIN CostAccount ca ON ca.transaction.id = t.id
        WHERE t.booking.id = :bookingId
        AND t.category = 'PAYMENT'
        AND ca.status = 'PAID'
    """)
    Double getTotalPaidForBooking(@Param("bookingId") Long bookingId);
}
