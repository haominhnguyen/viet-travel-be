package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourDayRepository extends JpaRepository<TourDay, Long> {

    @Query("SELECT td FROM TourDay td WHERE td.tour.id = :tourId AND td.deleted = false ORDER BY td.id")
    List<TourDay> findByTourIdOrderById(Long tourId);

    @Query("SELECT COUNT(td) > 0 FROM TourDay td WHERE td.tour.id = :tourId AND td.deleted = false")
    boolean existsActiveTourDaysByTourId(Long tourId);
}
