package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.model.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long>, JpaSpecificationExecutor<TourSchedule> {
    @Query("""
    SELECT new com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO(
        ts.id, 
        ts.startDate, 
        ts.endDate, 
        tp.sellingPrice, 
        tp.minPax, 
        tp.maxPax,
        (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)),
        ts.meetingLocation,
        ts.departureTime,
        tp.extraHotelCost
    ) 
    FROM TourSchedule ts
    JOIN ts.tour t
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status != "PENDING"
    WHERE t.id = :tourId
    GROUP BY ts.id, ts.startDate, ts.endDate, tp.sellingPrice, tp.minPax, tp.maxPax,
     ts.meetingLocation, ts.departureTime, tp.extraHotelCost
    ORDER BY ts.startDate ASC
""")
    List<PublicTourScheduleDTO> findTourScheduleBasicByTourId(@Param("tourId") Long tourId);


    @Query("""
    SELECT ts.id, (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0))
    FROM TourSchedule ts
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id AND tb.status != "PENDING"
    WHERE ts.id IN :scheduleIds AND ts.deleted = FALSE 
    GROUP BY ts.id, tp.maxPax
""")
    List<Object[]> findAvailableSeatsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    @Query("""
    SELECT new com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO(
        ts.id, 
        ts.startDate, 
        ts.endDate, 
        tp.sellingPrice, 
        tp.minPax, 
        tp.maxPax,
        (tp.maxPax - COALESCE(CAST(SUM(tb.seats) AS integer), 0)),
        ts.meetingLocation,
        ts.departureTime,
        tp.extraHotelCost
    ) 
    FROM TourSchedule ts
    JOIN ts.tour t
    JOIN ts.tourPax tp
    LEFT JOIN TourBooking tb ON tb.tourSchedule.id = ts.id
    WHERE t.id = :tourId AND ts.id = :tourScheduleId
    GROUP BY ts.id, ts.startDate, ts.endDate, tp.sellingPrice, tp.minPax, tp.maxPax,
     ts.meetingLocation, ts.departureTime, tp.extraHotelCost
    ORDER BY ts.startDate ASC
""")
    PublicTourScheduleDTO findTourScheduleByTourId(@Param("tourId") Long tourId, @Param("tourScheduleId") Long tourScheduleId);


}
