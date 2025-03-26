package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourPax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourPaxRepository extends JpaRepository<TourPax, Long> {

    List<TourPax> findByTourIdOrderByMinPax(Long tourId);

    @Query("SELECT tp FROM TourPax tp WHERE tp.tour.id = :tourId " +
            "AND :paxCount BETWEEN tp.minPax AND tp.maxPax " +
            "AND CURRENT_DATE BETWEEN tp.validFrom AND tp.validTo")
    List<TourPax> findByTourIdAndPaxRange(@Param("tourId") Long tourId, @Param("paxCount") Integer paxCount);

    boolean existsByTourIdAndMinPaxAndMaxPaxAndDeletedFalse(Long tourId, Integer minPax, Integer maxPax);

    List<TourPax> findByTourIdAndDeletedFalseOrderByMinPax(Long tourId);
}
