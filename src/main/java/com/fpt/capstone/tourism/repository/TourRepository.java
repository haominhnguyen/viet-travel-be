package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.TopRevenueTourDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {
    @Query("""
            SELECT tb.tour.id FROM TourBooking tb 
            JOIN TourSchedule ts ON ts.tour.id = tb.tour.id
            JOIN TourPax tp ON tp.tour.id = tb.tour.id
            WHERE YEAR(tb.createdAt) = YEAR(CURRENT_DATE)
            AND tb.tour.deleted = FALSE 
            AND tb.tour.tourType = 'SIC' 
            AND ts.tour.tourStatus = 'OPENED'
            AND ts.status = 'OPEN'
            AND ts.startDate > CURRENT_DATE 
            AND tp.validTo > CURRENT_DATE 
            GROUP BY tb.tour.id 
            ORDER BY COUNT(tb.tour.id) DESC
            """)
    List<Long> findTopTourIdsOfCurrentYear();

    @Query("""
            SELECT tb.tour.id FROM TourBooking tb
            GROUP BY tb.tour.id
            ORDER BY COUNT(tb.tour.id) DESC
            """)
    List<Long> findTrendingTourIds();
    @Query("""
            SELECT DISTINCT t FROM Tour t
            JOIN t.tourSchedules ts
            JOIN t.tourPax tp
            WHERE t.deleted = FALSE
            AND t.tourType = 'SIC'
            AND t.tourStatus = 'OPENED'
            AND ts.status = 'OPEN'
            AND ts.startDate > CURRENT_DATE
            AND tp.validTo > CURRENT_DATE
            AND t.id IN (:trendingTourIds)
            """)
    List<Tour> findPublicTourByIds(List<Long> trendingTourIds, Pageable pageable);

    @Query("""
            SELECT t FROM Tour t 
            JOIN TourSchedule ts on t.id = ts.tour.id
            JOIN TourPax tp ON t.id = tp.tour.id
            WHERE t.deleted = FALSE 
            AND t.tourType = 'SIC' 
            AND t.tourStatus = 'OPENED'
            AND ts.status = 'OPEN'
            AND ts.startDate > CURRENT_DATE 
            AND tp.validTo > CURRENT_DATE 
            ORDER BY t.createdAt DESC LIMIT 1
            """)
    Tour findNewestTour();


    @Query("""
                SELECT MIN(ts.tourPax.sellingPrice)
                FROM TourSchedule ts
                WHERE ts.tour.id = :tourId
                GROUP BY ts.tour.id
            """)
    Double findMinSellingPriceForTours(@Param("tourId") Long tourId);


    @Query(value = """
                SELECT t.id
                FROM tour t
                JOIN tour_location tl ON t.id = tl.tour_id
                JOIN tour_schedule ts ON ts.tour_id = t.id
                JOIN tour_pax tp ON tp.tour_id = t.id
                WHERE tl.location_id IN (:locationIds) AND t.is_deleted = FALSE
                AND t.tour_status IN ('OPENED')
                AND t.tour_type IN ('SIC')
                AND ts.status IN ('OPEN')
                AND ts.start_date > CURRENT_DATE
                AND tp.valid_to > CURRENT_DATE
                GROUP BY t.id
                ORDER BY RANDOM()
                LIMIT 3;
            """, nativeQuery = true)
    List<Long> findSameLocationTourIds(@Param("locationIds") List<Long> locationIds);


    @Query("""
                SELECT ts.tour.id, MIN(ts.tourPax.sellingPrice)
                FROM TourSchedule ts
                WHERE ts.tour.id IN :tourIds
                GROUP BY ts.tour.id
            """)
    List<Object[]> findMinSellingPrices(@Param("tourIds") List<Long> tourIds);

    @Query("""
            SELECT t FROM Tour t
            JOIN TourSchedule ts ON ts.tour.id = t.id AND ts.id =:scheduleId
            """)
    Tour findByScheduleId(@Param("scheduleId")Long scheduleId);


    List<Tour> findByNameContainingAndTourType(@Param("name") String name, @Param("tourType") TourType tourType);


    List<Tour> findByTourType(TourType tourType);

    List<Tour> findByNameContaining(String name);

    Tour findByName(String name);

    @Query("""
            SELECT t FROM Tour t
            JOIN TourSchedule ts ON t.id = ts.tour.id
            JOIN TourPax tp ON t.id = tp.tour.id
            WHERE t.tourType = 'SIC'
            AND t.deleted = FALSE
            AND t.tourStatus = 'OPENED'
            AND ts.status = 'OPEN'
            AND ts.startDate > CURRENT_DATE 
            AND tp.validTo > CURRENT_DATE 
            """)
    List<Tour> findAllPublicTour();

    @Query("""
                SELECT new com.fpt.capstone.tourism.dto.common.TopRevenueTourDTO(
                t.id, 
                t.name,
                t.tourType,
                CAST( SUM (ca.finalAmount) AS BIGDECIMAL ) 
            )
            FROM TourBooking tb
            JOIN tb.tour t
            JOIN Transaction tr ON tb.id = tr.booking.id
            JOIN CostAccount ca ON tr.id = ca.transaction.id
            WHERE DATE(ca.createdAt) BETWEEN :startDate AND :endDate 
            AND tr.category IN :transactionTypes
            AND ca.status = :paid
            GROUP BY t.id, t.name, t.tourType
            ORDER BY CAST( SUM (ca.finalAmount) AS BIGDECIMAL )  DESC 
            """)
    List<TopRevenueTourDTO> getTopRevenueTourByMonth(LocalDate startDate, LocalDate endDate, List<TransactionType> transactionTypes, CostAccountStatus paid, Pageable pageable);

    @Query(value = "SELECT tour_type FROM tour t WHERE t.id = :tourId", nativeQuery = true)
    TourType getTourTypeByTourId(@Param("tourId") Long tourId);


    Tour findByIdAndTourStatusAndTourType(Long id, TourStatus tourStatus, TourType tourType);

    @Query(value = "SELECT * FROM tour t WHERE t.id = :tourId", nativeQuery = true)
    Tour findTourByTourId(@Param("tourId") Long tourId);

    @Query(value = """
    SELECT id, name FROM tour WHERE id IN (:ids)
""", nativeQuery = true)
    List<Object[]> findToursByIds(@Param("ids") List<Long> ids);

    @Query(value = """
            SELECT tour_type FROM tour t 
            JOIN tour_booking tb on t.id = tb.tour_id
            JOIN tour_booking_service tbs on tbs.tour_booking_id = tb.id
            WHERE tbs.id = :tourBookingServiceId
            """, nativeQuery = true)
    TourType findTourTypeByTourBookingServiceId(Long tourBookingServiceId);

    @Query(value = """
            SELECT tour_type FROM tour t 
            JOIN tour_booking tb on t.id = tb.tour_id
            WHERE tb.id = :bookingId
            """, nativeQuery = true)
    TourType findTourTypeByTourBookingId(Long bookingId);
}
