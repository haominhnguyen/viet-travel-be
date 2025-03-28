package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourDayService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourDayServiceRepository extends JpaRepository<TourDayService, Long> {
    @Query("SELECT tds.service.id FROM TourDayService tds WHERE tds.tourDay.id = :tourDayId")
    List<Long> findServiceIdsByTourDayId(@Param("tourDayId") Long tourDayId);
    List<TourDayService> findByTourDayId(Long tourDayId);

    Optional<TourDayService> findByTourDayIdAndServiceId(Long tourDayId, Long serviceId);

    TourDayService findByServiceId(Long serviceId);

    List<TourDayService> findByTourDayIdIn(List<Long> tourDayIds);

    Optional<TourDayService> findByServiceIdAndTourDayTourId(Long serviceId, Long tourId);

    boolean existsByServiceIdAndTourDayId(Long serviceId, Long id);

    @Query("SELECT tds FROM TourDayService tds JOIN tds.tourDay td WHERE tds.service.id = :serviceId AND td.tour.id = :tourId")
    List<TourDayService> findAllByServiceIdAndTourId(
            @Param("serviceId") Long serviceId,
            @Param("tourId") Long tourId);

    // Add this method if you want to use it elsewhere
    @Query("SELECT tds FROM TourDayService tds JOIN tds.tourDay td WHERE tds.service.id = :serviceId AND td.tour.id = :tourId ORDER BY td.dayNumber ASC")
    List<TourDayService> findByServiceIdAndTourIdOrderByDayNumberAsc(
            @Param("serviceId") Long serviceId,
            @Param("tourId") Long tourId);

    // If you need to limit to just one result
    @Query(value = "SELECT tds.* FROM tour_day_service tds " +
            "JOIN tour_day td ON tds.tour_day_id = td.id " +
            "WHERE tds.service_id = :serviceId AND td.tour_id = :tourId " +
            "ORDER BY td.day_number ASC LIMIT 1",
            nativeQuery = true)
    Optional<TourDayService> findFirstByServiceIdAndTourId(
            @Param("serviceId") Long serviceId,
            @Param("tourId") Long tourId);
}

