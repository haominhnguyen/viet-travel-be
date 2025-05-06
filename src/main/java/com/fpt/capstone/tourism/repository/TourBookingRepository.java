package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.RecentBookingDTO;
import com.fpt.capstone.tourism.dto.common.RefundDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourTypeRatioDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long>, JpaSpecificationExecutor<TourBooking> {
    TourBooking findByBookingCode(String bookingCode);

    /**
     * Find tour by booking id
     * @param bookingId bookId
     * @return tour booking data
     */
    @Query(value = "select * from tour_booking where id = ?1", nativeQuery = true)
    TourBooking findByBookingId(Long bookingId);

    List<TourBooking> findByTourSchedule_Id(Long scheduleId);

    @Query("""
    SELECT tb FROM TourBooking tb
    JOIN TourBookingService tbs ON tb.id = tbs.booking.id
    WHERE tb.tourSchedule.id = :scheduleId
    AND tbs.service.id = :serviceId
""")
    List<TourBooking> findByTourScheduleIdAndServiceId(@Param("scheduleId") Long scheduleId, @Param("serviceId") Long serviceId);


    @Query(value = """
    SELECT COALESCE(count(tb.id), 0) FROM TourBooking tb
    JOIN TourBookingCustomer tbc ON tb.id = tbc.tourBooking.id
    AND tbc.ageType = 'ADULT' 
    AND tbc.bookedPerson = false 
    WHERE tbc.tourBooking.id = :id
""")
    Integer countAdultNumberByBookingId(@Param("id") Long id);
    @Query(value = """
    SELECT COALESCE(count(tb.id), 0) FROM TourBooking tb
    JOIN TourBookingCustomer tbc ON tb.id = tbc.tourBooking.id
    AND tbc.ageType = 'CHILDREN'
    WHERE tbc.tourBooking.id = :id
""")
    Integer countChildNumberByBookingId(@Param("id")Long id);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
             FROM Transaction t 
             JOIN CostAccount ca ON t.id = ca.transaction.id AND ca.status = "PAID"
             WHERE t.booking.id = :id AND t.category = 'RECEIPT'
             """)
    Double findReceiptAmountByBookingId(@Param("id")Long id);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
             FROM Transaction t 
             JOIN CostAccount ca ON t.id = ca.transaction.id AND ca.status = "PAID"
             WHERE t.booking.id = :id AND t.category = 'COLLECTION'
             """)
    Double findCollectionAmountByBookingId(Long id);
    long countByTourAndStatusIn(Tour tour, List<TourBookingStatus> tourBookingStatuses);


    List<TourBooking> findAllByTourAndTourSchedule(Tour tour, TourSchedule tourSchedule);

    @Query("""
        SELECT tb.id FROM TourBooking tb
        JOIN TourBookingService tbs on tb.id = tbs.booking.id
        WHERE tbs.service.id = :serviceId
    """)
    Long findByServiceId(Long serviceId);


    @Query("""
        SELECT tb.id, tb.bookingCode, 
           tc.fullName, tc.phoneNumber, tc.email, tc.address
        FROM TourBooking tb 
        JOIN tb.customers tc 
        WHERE tb.bookingCode LIKE %:bookingCode% 
        AND tc.bookedPerson = true
    """)
    List<Object[]> findByBookingCodeContaining(@Param("bookingCode") String keyword);

    @Query("SELECT COUNT(b) FROM TourBooking b WHERE b.tourSchedule.id = :scheduleId AND b.status <> 'CANCELLED'")
    Integer countByTourScheduleIdAndStatusNot(Long scheduleId);

    @Query("""
                SELECT new com.fpt.capstone.tourism.dto.common.TourTypeRatioDTO(
                EXTRACT(MONTH FROM tb.createdAt),
                EXTRACT(YEAR FROM tb.createdAt),
                COALESCE(SUM(CASE WHEN t.tourType = 'SIC' THEN 1 ELSE 0 END) * 100.0 / COUNT(tb.id), 0),
                COALESCE(SUM(CASE WHEN t.tourType = 'PRIVATE' THEN 1 ELSE 0 END) * 100.0 / COUNT(tb.id), 0)
            )
            FROM TourBooking tb
            JOIN tb.tour t
            WHERE DATE(tb.createdAt) BETWEEN :startDate AND :endDate 
            AND tb.status = :success
            GROUP BY EXTRACT(YEAR FROM tb.createdAt), EXTRACT(MONTH FROM tb.createdAt) 
            ORDER BY EXTRACT(YEAR FROM tb.createdAt) DESC, EXTRACT(MONTH FROM tb.createdAt) DESC 
            """)
    List<TourTypeRatioDTO> getTourTypeRatioByMonth(LocalDate startDate, LocalDate endDate, TourBookingStatus success);

    @Query("""
                SELECT new com.fpt.capstone.tourism.dto.common.RecentBookingDTO(
                tb.id, 
                u.fullName,
                t.name,
                CAST( tb.totalAmount AS BIGDECIMAL),
                tb.createdAt
            )
            FROM TourBooking tb
            JOIN tb.user u
            JOIN tb.tour t
            WHERE DATE(tb.createdAt) BETWEEN :startDate AND :endDate 
            ORDER BY tb.createdAt DESC 
            """)
    List<RecentBookingDTO> getRecentBooking(LocalDate startDate, LocalDate endDate, Pageable i);

    @Query("""
            SELECT COUNT (*)
            FROM TourBooking tb
            WHERE DATE(tb.createdAt) BETWEEN :startDate AND :endDate 
            AND tb.status IN :tourBookingStatus
            """)
    Integer getBookingNumberByStatus(LocalDate startDate, LocalDate endDate, List<TourBookingStatus> tourBookingStatus);

    @Query("""
            SELECT COUNT (*)
            FROM TourBooking tb
            WHERE DATE(tb.createdAt) BETWEEN :startDate AND :endDate 
            AND tb.tourBookingCategory = :tourBookingCategory
            """)
    Integer getBookingNumberByType(LocalDate startDate, LocalDate endDate, TourBookingCategory tourBookingCategory);

    @Query("""
    SELECT COUNT (*) FROM (
    SELECT tb.user.id AS user_id
    FROM TourBooking tb 
    WHERE DATE(tb.createdAt) BETWEEN :startDate AND :endDate 
    GROUP BY tb.user.id 
    HAVING COUNT(tb.id) >= 2
    ) AS subquery
""")
    Integer getReturnCustomerNumber(LocalDate startDate, LocalDate endDate);

    @Query("""
    SELECT b FROM TourBooking b
    JOIN FETCH b.sale
    JOIN FETCH b.tour
    LEFT JOIN FETCH b.transactions
    WHERE b.tourSchedule.id = :id
""")
    List<TourBooking> findBookingWithoutCustomersByScheduleId(@Param("id") Long id);

    @Query("""
                SELECT new com.fpt.capstone.tourism.dto.common.RefundDetailDTO(
                tb.id, 
                t.name,
                tb.bookingCode,
                ts.startDate,
                ts.endDate,
                tr.amount,
                tr.notes,
                tr.category,
                tr.transactionStatus,
                tb.user.fullName,
                tr.receivedBy,
                tr.paidBy
            )
            FROM TourBooking tb
            JOIN tb.tour t
            JOIN tb.tourSchedule ts
            JOIN tb.transactions tr
            WHERE tb.id = :tourBookingId
            AND tb.status = :requestCancelledWithRefund
            """)
    List<RefundDetailDTO> findDetailRefundRequestByBookingId(Long tourBookingId, TourBookingStatus requestCancelledWithRefund);


    List<TourBooking> findByStatusAndExpiredAtBeforeAndDeletedFalse(TourBookingStatus status, LocalDateTime now);

    @Query("""
    SELECT tb FROM TourBooking tb
    WHERE tb.tourSchedule.id = :scheduleId
    AND tb.status = :tourBookingStatus
""")
    List<TourBooking> findBookingByStatusAndTourSchedule_Id(TourBookingStatus tourBookingStatus, Long scheduleId);

    boolean existsByTourIdAndStatusIn(Long tourId, List<TourBookingStatus> statuses);
}

