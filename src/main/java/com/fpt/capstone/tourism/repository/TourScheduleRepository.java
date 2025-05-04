package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long>, JpaSpecificationExecutor<TourSchedule> {
    @Query("""
    SELECT new com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO(
        ts.id, 
        ts.startDate, 
        ts.endDate, 
        tp.sellingPrice, 
        tp.minPax, 
        tp.maxPax,
        (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)),
        ts.meetingLocation,
        ts.departureTime,
        tp.extraHotelCost
    ) 
    FROM TourSchedule ts
    JOIN ts.tour t
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "SUCCESS"
    WHERE t.id = :tourId 
    AND ts.status = 'OPEN'
    GROUP BY ts.id, ts.startDate, ts.endDate, tp.sellingPrice, tp.minPax, tp.maxPax,
     ts.meetingLocation, ts.departureTime, tp.extraHotelCost
    ORDER BY ts.startDate ASC
""")
    List<PublicTourScheduleDTO> findTourScheduleBasicByTourId(@Param("tourId") Long tourId);

    @Query("""
    SELECT new com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO(
        ts.id, 
        ts.startDate, 
        ts.endDate, 
        tp.sellingPrice, 
        tp.minPax, 
        tp.maxPax,
        (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)),
        ts.meetingLocation,
        ts.departureTime,
        tp.extraHotelCost
    ) 
    FROM TourSchedule ts
    JOIN ts.tour t
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "SUCCESS"
    WHERE t.id = :tourId and ts.id != :tourScheduleId AND ts.status = 'OPEN'
    GROUP BY ts.id, ts.startDate, ts.endDate, tp.sellingPrice, tp.minPax, tp.maxPax,
     ts.meetingLocation, ts.departureTime, tp.extraHotelCost
     HAVING (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)) >= :seats
    ORDER BY ts.startDate ASC
""")
    List<PublicTourScheduleDTO> findTourScheduleBasicByTourIdAndNotEqualScheduleId(@Param("tourId") Long tourId, @Param("tourScheduleId") Long tourScheduleId, @Param("seats") int seats);


    @Query("""
    SELECT ts.id, (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0))
    FROM TourSchedule ts
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "SUCCESS"
    WHERE ts.id IN :scheduleIds AND ts.deleted = FALSE 
    GROUP BY ts.id, tp.maxPax
""")
    List<Object[]> findAvailableSeatsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    @Query("""
    SELECT new com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO(
        ts.id,
        ts.startDate,
        ts.endDate,
        tp.sellingPrice,
        tp.minPax,
        tp.maxPax,
        (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)),
        ts.meetingLocation,
        ts.departureTime,
        tp.extraHotelCost
    ) 
    FROM TourSchedule ts
    JOIN ts.tour t
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "SUCCESS"
    WHERE t.id = :tourId AND ts.id = :tourScheduleId
    GROUP BY ts.id, ts.startDate, ts.endDate, tp.sellingPrice, tp.minPax, tp.maxPax,
     ts.meetingLocation, ts.departureTime, tp.extraHotelCost
    ORDER BY ts.startDate ASC
""")
    PublicTourScheduleDTO findTourScheduleByTourId(@Param("tourId") Long tourId, @Param("tourScheduleId") Long tourScheduleId);


    @Query("""
    SELECT COALESCE(CAST(SUM(tb.seats) AS integer), 0)
    FROM TourSchedule ts
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "SUCCESS"
    WHERE ts.id IN :scheduleId AND ts.deleted = FALSE 
    GROUP BY ts.id
""")
    Integer findSoldSeatsByScheduleId(@Param("scheduleId")Long scheduleId);

    @Query("""
    SELECT COALESCE(CAST(SUM(tb.seats) AS integer), 0)
    FROM TourSchedule ts
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status = "PENDING"
    WHERE ts.id IN :scheduleId AND ts.deleted = FALSE 
    GROUP BY ts.id
""")
    Integer findPendingSeatsByScheduleId(@Param("scheduleId")Long scheduleId);

    @Query("""
    SELECT SUM(COALESCE(tb.sellingPrice, 0) + COALESCE(tb.extraHotelCost, 0))
     FROM TourBooking tb WHERE tb.tourSchedule.id = :scheduleId
     AND tb.status = "SUCCESS"
""")
    Double findTotalTourCostByScheduleId(@Param("scheduleId")Long scheduleId);

    @Query("""
    SELECT SUM(COALESCE(t.amount, 0)) 
    FROM Transaction t 
    JOIN TourBooking tb ON t.booking.id = tb.id AND tb.tourSchedule.id = :scheduleId
    WHERE t.category IN (com.fpt.capstone.tourism.model.enums.TransactionType.PAYMENT,\s
                         com.fpt.capstone.tourism.model.enums.TransactionType.ADVANCED)
""")
    Double findPaidTourCostByScheduleId(@Param("scheduleId")Long scheduleId);

    @Query("""
    SELECT SUM(COALESCE(t.amount, 0)) 
    FROM Transaction t 
    JOIN TourBooking tb ON t.booking.id = tb.id AND tb.tourSchedule.id = :scheduleId
    WHERE t.category IN (com.fpt.capstone.tourism.model.enums.TransactionType.RECEIPT,\s
                         com.fpt.capstone.tourism.model.enums.TransactionType.COLLECTION)
""")
    Double findRevenueCostByScheduleId(@Param("scheduleId")Long scheduleId);

    @Query("SELECT COUNT(ts) FROM TourSchedule ts " +
            "WHERE ts.operator.id = :operatorId " +
            "AND ts.deleted = false " +
            "AND ts.status IN ('ONGOING','DRAFT','OPEN_FOR_BOOKING','OPEN') " +
            "AND ((ts.startDate BETWEEN :startDate AND :endDate) " +
            "OR (ts.endDate BETWEEN :startDate AND :endDate) " +
            "OR (:startDate BETWEEN ts.startDate AND ts.endDate))")
    int countActiveToursForOperator(
            @Param("operatorId") Long operatorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query("""
    SELECT ts
    FROM TourSchedule ts
    JOIN TourBooking tb ON ts.id = tb.tourSchedule.id 
    JOIN TourBookingService tbs ON tbs.booking.id = tb.id
    WHERE tbs.id = :tourBookingServiceId
""")
    TourSchedule findByTourBookingServiceId(Long tourBookingServiceId);

    @Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TourSchedule ts " +
            "WHERE ts.tour.id = :tourId " +
            "AND ts.operator.id = :operatorId " +
            "AND ts.deleted = false " +
            "AND ts.status <> 'CANCELLED' " +
            "AND ((ts.startDate <= :endDate AND ts.endDate >= :startDate))")
    boolean existsByTourIdAndOperatorIdAndDateOverlap(@Param("tourId") Long tourId,
                                                      @Param("operatorId") Long operatorId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);


    @Query("SELECT COUNT(ts) > 0 FROM TourSchedule ts " +
            "WHERE ts.tour.id = :tourId " +
            "AND ts.operator.id = :operatorId " +
            "AND ts.id != :excludeId " +
            "AND ts.deleted = false " +
            "AND (" +
            "    (:startDate BETWEEN ts.startDate AND ts.endDate) OR " +
            "    (:endDate BETWEEN ts.startDate AND ts.endDate) OR " +
            "    (ts.startDate BETWEEN :startDate AND :endDate) OR " +
            "    (ts.endDate BETWEEN :startDate AND :endDate)" +
            ")")
    boolean existsByTourIdAndOperatorIdAndDateOverlapExcludingId(
            @Param("tourId") Long tourId,
            @Param("operatorId") Long operatorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(ts) FROM TourSchedule ts " +
            "WHERE ts.operator.id = :operatorId " +
            "AND ts.id != :excludeId " +
            "AND ts.deleted = false " +
            "AND ts.status IN ('CONFIRMED', 'IN_PROGRESS') " +
            "AND (" +
            "    (:startDate BETWEEN ts.startDate AND ts.endDate) OR " +
            "    (:endDate BETWEEN ts.startDate AND ts.endDate) OR " +
            "    (ts.startDate BETWEEN :startDate AND :endDate) OR " +
            "    (ts.endDate BETWEEN :startDate AND :endDate)" +
            ")")
    int countActiveToursForOperatorExcludingId(
            @Param("operatorId") Long operatorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeId") Long excludeId);

    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.deleted = false")
    List<TourSchedule> findActiveTourSchedulesByTourId(@Param("tourId") Long tourId);

    @Query("SELECT ts FROM TourSchedule ts WHERE ts.status = :status AND ts.deleted = false")
    List<TourSchedule> findSettlementTourScheduleByStatus(@Param("status")  TourScheduleStatus status);


    @Query("""
    SELECT ts FROM TourSchedule ts
    JOIN FETCH ts.tour t
    WHERE ts.id = :id
""")
    Optional<TourSchedule> findScheduleWithBookings(@Param("id") Long id);
@Query("""
        SELECT ts.tourPax.id FROM TourSchedule ts WHERE  ts.id = :id
        """)
    int findTourPaxIdByScheduleId(@Param("id") Long id);
}
