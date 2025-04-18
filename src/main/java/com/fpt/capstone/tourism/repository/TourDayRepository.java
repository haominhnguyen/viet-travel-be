package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.enums.TourType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourDayRepository extends JpaRepository<TourDay, Long> {

    @Query("SELECT td FROM TourDay td WHERE td.tour.id = :tourId AND td.deleted = false ORDER BY td.id")
    List<TourDay> findByTourIdOrderById(Long tourId);

    @Query("SELECT COUNT(td) > 0 FROM TourDay td WHERE td.tour.id = :tourId AND td.deleted = false")
    boolean existsActiveTourDaysByTourId(Long tourId);


    Optional<TourDay> findByIdAndTourId(Long id, Long tourId);
    List<TourDay> findByTourIdOrderByDayNumber(Long tourId);
    List<TourDay> findByTourIdAndDeletedOrderByDayNumber(Long tourId, Boolean deleted);
    @Query("SELECT MAX(td.dayNumber) FROM TourDay td WHERE td.tour.id = :tourId")
    Optional<Integer> findMaxDayNumberByTourId(@Param("tourId") Long tourId);

    @Query("SELECT COUNT(td) > 0 FROM TourDay td WHERE td.tour.id = :tourId AND td.dayNumber = :dayNumber AND td.id != :tourDayId")
    boolean existsByTourIdAndDayNumberAndIdNot(@Param("tourId") Long tourId, @Param("dayNumber") Integer dayNumber, @Param("tourDayId") Long tourDayId);


    List<TourDay> findByTourIdAndDeletedFalseOrderByDayNumber(Long tourId);
    Optional<TourDay> findByTourIdAndDayNumber(Long tourId, Integer dayNumber);

    List<TourDay> findAllByTourId(Long tourId);


    List<TourDay> findListTourDayByTourId(Long tourId);


    @Query("""
    SELECT td FROM TourDay td
    JOIN TourSchedule ts ON td.tour.id = ts.tour.id
    WHERE ts.id = :scheduleId
""")
    List<TourDay> findListTourDayByScheduleId(Long scheduleId);
}
