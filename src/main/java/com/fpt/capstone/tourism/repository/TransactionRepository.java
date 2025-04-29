package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findAllByBookingAndCategoryIn(TourBooking tourBooking, List<TransactionType> transactionType);



    List<Transaction> findAllByBookingIn(List<TourBooking> tourBookings);

    List<Transaction> findByBooking_Id(Long bookingId);

    @Query("""
        SELECT COALESCE(SUM(ca.finalAmount), 0)
        FROM Transaction t
        JOIN CostAccount ca ON ca.transaction.id = t.id
        JOIN TourBooking tb ON t.booking.id = tb.id
        JOIN TourBookingService tbs ON tbs.booking.id = tb.id
        WHERE tbs.id = :bookingId AND tbs.service.id = :serviceId
        AND t.category = 'PAYMENT'
        AND ca.status = 'PAID'
        GROUP BY tbs.id
    """)
    Double getTotalPaidForBooking(@Param("bookingId") Long bookingId, Long serviceId);

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
        AND t.category In :transactionType
    """)
    BigDecimal findTotalAmountByTransactionCategoryIn(List<Transaction> transactions, List<TransactionType> transactionType);

    @Query("""
        SELECT CAST(COALESCE(SUM(t.amount), 0) AS bigdecimal)
        FROM Transaction t 
        JOIN CostAccount ca ON ca.transaction.id = t.id
        WHERE t IN :transactions
        AND t.category In :transactionTypes
    """)
    BigDecimal findEstimateReceiptAmount(List<Transaction> transactions, List<TransactionType> transactionTypes);
}
