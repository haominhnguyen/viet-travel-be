package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourDayServiceCategoryRepository extends JpaRepository<TourDayServiceCategory, Long> {
    List<TourDayServiceCategory> findByTourDayId(Long tourDayId);
    void deleteByTourDayId(Long tourDayId);

    void deleteByTourDay(TourDay tourDay);

    List<TourDayServiceCategory> findByTourDay(TourDay tourDay);
}
