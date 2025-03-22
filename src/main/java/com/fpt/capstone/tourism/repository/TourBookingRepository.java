package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking, Long>, JpaSpecificationExecutor<TourBooking> {
    TourBooking findByBookingCode(String bookingCode);

    List<TourBooking> findByTourSchedule_Id(Long scheduleId);

    @Query("""
    SELECT tb FROM TourBooking tb
    JOIN TourBookingService tbs ON tb.id = tbs.booking.id
    WHERE tb.tourSchedule.id = :scheduleId
    AND tbs.service.id = :serviceId
""")
    List<TourBooking> findByTourScheduleIdAndServiceId(@Param("scheduleId") Long scheduleId, @Param("serviceId") Long serviceId);


    @Query(value = """
    SELECT COALESCE(count(tb.id), 0) FROM TourBooking tb
    JOIN TourBookingCustomer tbc ON tb.id = tbc.tourBooking.id
    AND tbc.ageType = 'ADULT'
    WHERE tbc.tourBooking.id = :id
""")
    Integer countAdultNumberByBookingId(@Param("id") Long id);
    @Query(value = """
    SELECT COALESCE(count(tb.id), 0) FROM TourBooking tb
    JOIN TourBookingCustomer tbc ON tb.id = tbc.tourBooking.id
    AND tbc.ageType = 'CHILDREN'
    WHERE tbc.tourBooking.id = :id
""")
    Integer countChildNumberByBookingId(@Param("id")Long id);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
             FROM Transaction t 
             JOIN CostAccount ca ON t.id = ca.transaction.id AND ca.status = "PAID"
             WHERE t.booking.id = :id AND t.category = 'RECEIPT'
             """)
    Double findReceiptAmountByBookingId(@Param("id")Long id);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
             FROM Transaction t 
             JOIN CostAccount ca ON t.id = ca.transaction.id AND ca.status = "PAID"
             WHERE t.booking.id = :id AND t.category = 'COLLECTION'
             """)
    Double findCollectionAmountByBookingId(Long id);
    long countByTourAndStatusIn(Tour tour, List<TourBookingStatus> tourBookingStatuses);


    List<TourBooking> findAllByTourAndTourSchedule(Tour tour, TourSchedule tourSchedule);

    @Query("""
        SELECT tb.id FROM TourBooking tb
        JOIN TourBookingService tbs on tb.id = tbs.booking.id
        WHERE tbs.service.id = :serviceId
    """)
    Long findByServiceId(Long serviceId);

}
