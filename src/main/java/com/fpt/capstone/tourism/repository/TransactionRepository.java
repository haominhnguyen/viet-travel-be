package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByBookingAndCategoryIn(TourBooking tourBooking, List<TransactionType> transactionType);



    List<Transaction> findAllByBookingIn(List<TourBooking> tourBookings);

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

    @Query("""
        SELECT CAST(COALESCE(SUM(ca.finalAmount), 0) AS bigdecimal)
        FROM Transaction t 
        JOIN CostAccount ca ON ca.transaction.id = t.id
        WHERE t IN :transactions
        AND t.category = :transactionType
        AND ca.status = :costAccountStatus
    """)
    BigDecimal findAmountByTransactionCategoryAndCostAccountStatusIn(List<Transaction> transactions, TransactionType transactionType, CostAccountStatus costAccountStatus);

    @Query("""
        SELECT CAST(COALESCE(SUM(ca.finalAmount), 0)AS bigdecimal)
        FROM Transaction t 
        JOIN CostAccount ca ON ca.transaction.id = t.id
        WHERE t IN :transactions
        AND t.category = :transactionType
        AND ca.status = :costAccountStatus
    """)
    BigDecimal findTotalAmountByTransactionCategoryAndCostAccountStatus(
            List<Transaction> transactions, TransactionType transactionType, CostAccountStatus costAccountStatus
    );
    @Query("""
        SELECT CAST(COALESCE(SUM(t.amount), 0) AS bigdecimal)
        FROM Transaction t 
        WHERE t IN :transactions
        AND t.category = :transactionType
    """)
    BigDecimal findTotalAmountByTransactionCategoryIn(List<Transaction> transactions, TransactionType transactionType);
}
