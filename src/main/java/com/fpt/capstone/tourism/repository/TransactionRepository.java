package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByBookingAndCategory(TourBooking tourBooking, TransactionType transactionType);


    @Query("""
            SELECT t FROM Transaction t
                WHERE t.booking.tourSchedule.id = :scheduleId
            """)
    List<Transaction> findAllByTourScheduleId(@Param("scheduleId") Long scheduleId);
}
