package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourDayServiceCategoryRepository extends JpaRepository<TourDayServiceCategory, Long> {
    List<TourDayServiceCategory> findByTourDayId(Long tourDayId);
    void deleteByTourDayId(Long tourDayId);

    void deleteByTourDay(TourDay tourDay);

    List<TourDayServiceCategory> findByTourDay(TourDay tourDay);


    @Query("SELECT DISTINCT tds.serviceCategory FROM TourDayServiceCategory tds " +
            "WHERE tds.tourDay.id IN :tourDayIds")
    List<ServiceCategory> findServiceCategoriesByTourDayIds(@Param("tourDayIds") List<Long> tourDayIds);


    @Query("SELECT sc, tds.tourDay FROM TourDayServiceCategory tds " +
            "JOIN tds.serviceCategory sc " +
            "JOIN tds.tourDay td " +
            "WHERE td.tour.id = :tourId")
    List<Object[]> findServiceCategoriesWithTourDaysByTourId(@Param("tourId") Long tourId);
}
