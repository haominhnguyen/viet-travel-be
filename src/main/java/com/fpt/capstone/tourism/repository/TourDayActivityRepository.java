//package com.fpt.capstone.tourism.repository;
//
//import com.fpt.capstone.tourism.model.TourDayActivity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface TourDayActivityRepository extends JpaRepository<TourDayActivity, Long> {
//    List<TourDayActivity> findByTourDayTourIdAndDeletedFalse(Long tourId);
//    Optional<TourDayActivity> findByActivityIdAndTourDayTourId(Long activityId, Long tourId);
//}
