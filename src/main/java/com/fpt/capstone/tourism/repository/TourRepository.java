package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.Tour;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public interface TourRepository  extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {
    @Query("SELECT tb.tour.id FROM TourBooking tb " +
            "WHERE YEAR(tb.bookingDate) = YEAR(CURRENT_DATE)" +
            "AND tb.tour.deleted = FALSE " +
            "AND tb.tour.opened = TRUE " +
            "GROUP BY tb.tour.id " +
            "ORDER BY COUNT(tb.tour.id) DESC")
    List<Long> findTopTourIdsOfCurrentYear();

    @Query("SELECT tb.tour.id FROM TourBooking tb " +
            "WHERE tb.tour.deleted = FALSE " +
            "AND tb.tour.opened = TRUE " +
            "GROUP BY tb.tour.id " +
            "ORDER BY COUNT(tb.tour.id) DESC")
    List<Long> findTrendingTourIds(Pageable pageable);

    @Query("SELECT t FROM Tour t WHERE t.deleted = FALSE AND t.opened = TRUE ORDER BY t.createdAt DESC LIMIT 1")
    Tour findNewestTour();



    @Query("""
    SELECT tp.tour.id, MIN(tp.sellingPrice) 
    FROM TourPax tp
    WHERE tp.tour.id = :tourId
    GROUP BY tp.tour.id
""")
    Double findMinSellingPriceForTours(@Param("tourId") Long tourId);


    @Query(value = """
    SELECT t.id
    FROM tour t
             JOIN tour_location tl ON t.id = tl.tour_id
    WHERE tl.location_id IN (:locationIds)
    GROUP BY t.id
    ORDER BY RANDOM()
    LIMIT 3;
""", nativeQuery = true)
    List<Long> findSameLocationTourIds(@Param("locationIds") List<Long> locationIds);

    @Query("""
    SELECT DISTINCT t FROM Tour t
    LEFT JOIN FETCH t.tourImages img
    LEFT JOIN FETCH t.depart_location loc
    WHERE t.id IN :ids
""")
    List<Tour> findSameLocationToursWithDetails(List<Long> tourIds);

    @Query("""
    SELECT tp.tour.id, MIN(tp.sellingPrice)
    FROM TourPax tp
    WHERE tp.tour.id IN :tourIds
    GROUP BY tp.tour.id
""")
    List<Object[]> findMinSellingPrices(@Param("tourIds") List<Long> tourIds);
}
