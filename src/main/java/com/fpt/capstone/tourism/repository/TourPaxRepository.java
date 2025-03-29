package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourPax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourPaxRepository extends JpaRepository<TourPax, Long> {

    List<TourPax> findByTourIdOrderByMinPax(Long tourId);

    @Query("SELECT tp FROM TourPax tp WHERE tp.tour.id = :tourId " +
            "AND :paxCount BETWEEN tp.minPax AND tp.maxPax " +
            "AND CURRENT_DATE BETWEEN tp.validFrom AND tp.validTo")
    List<TourPax> findByTourIdAndPaxRange(@Param("tourId") Long tourId, @Param("paxCount") Integer paxCount);

    boolean existsByTourIdAndMinPaxAndMaxPaxAndDeletedFalse(Long tourId, Integer minPax, Integer maxPax);

    List<TourPax> findByTourIdAndDeletedFalseOrderByMinPax(Long tourId);

    @Query("SELECT tp FROM TourPax tp WHERE tp.tour = :tour")
    Optional<TourPax> findByTourAndIsDefault(@Param("tour") Tour tour);

    @Query("SELECT tp FROM TourPax tp WHERE tp.tour = :tour")
    List<TourPax> findAllByTourAndIsDefault(@Param("tour") Tour tour, @Param("isDefault") boolean isDefault);


    @Query(value = "SELECT * FROM tour_pax WHERE tour_id = :tourId LIMIT 1", nativeQuery = true)
    Optional<TourPax> findDefaultByTourId(@Param("tourId") Long tourId);
    List<TourPax> findByTourIdAndIdNotAndDeletedFalseOrderByMinPax(Long tourId, Long paxId);


}
