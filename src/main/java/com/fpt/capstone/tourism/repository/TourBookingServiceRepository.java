package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.ChangeServiceDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import org.apache.catalina.LifecycleState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fpt.capstone.tourism.model.TourDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TourBookingServiceRepository extends JpaRepository<TourBookingService, Long>, JpaSpecificationExecutor<TourBookingService> {

    TourBookingService findByBookingIdAndServiceIdAndDeletedFalse(Long bookingId, Long serviceId);

    Page<TourBookingService> findByStatusIn(List<TourBookingServiceStatus> statuses, Pageable pageable);


    List<TourBookingService> findByTourDayAndBooking(TourDay tourDay, TourBooking tourBooking);

    Page<TourBookingService> findByRequestedQuantityGreaterThan(int i, Pageable pageable);

    Page<TourBookingService> findByRequestedQuantityGreaterThanOrStatus(int i, TourBookingServiceStatus tourBookingServiceStatus, Pageable pageable);

    @Query("""
                SELECT tb FROM TourBookingService tb
                JOIN FETCH tb.booking b
                JOIN FETCH b.tour t
                JOIN FETCH b.tourSchedule.operator u
                LEFT JOIN FETCH tb.service s
                LEFT JOIN FETCH tb.tourDay td
                WHERE tb.requestedQuantity > 0 OR tb.status in :statuses
                AND u.id = :userId
                ORDER BY tb.updatedAt desc 
            """)
    Page<TourBookingService> findByRequestedQuantityGreaterThanOrStatus(Long userId,
            @Param("statuses") List<TourBookingServiceStatus> statuses, Pageable pageable);

    @Query("""
            SELECT tbs FROM TourBookingService tbs 
            JOIN FETCH tbs.booking b
            JOIN FETCH b.tour t
            JOIN FETCH b.tourSchedule ts
            JOIN FETCH tbs.service s
            WHERE tbs.id = :id""")
    Optional<TourBookingService> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT tbs FROM TourBookingService tbs 
            JOIN FETCH tbs.booking b
            WHERE b.id = :bookingId""")
    List<TourBookingService> findByTourBookingId(@Param("bookingId") Long bookingId);

    List<TourBookingService> findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(Long bookingId, Long serviceId, Long tourDayId);

    @Query("""
            SELECT tbs FROM TourBookingService tbs 
            JOIN FETCH tbs.booking b
            WHERE b.tourSchedule.id = :id""")
    List<TourBookingService> findByScheduleId(Long id);
}
