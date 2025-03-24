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
}

