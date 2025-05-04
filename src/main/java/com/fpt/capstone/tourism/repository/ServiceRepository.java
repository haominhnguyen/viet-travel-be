package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicServiceDTO;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    @Query("SELECT s FROM Service s WHERE s.id = :id AND s.serviceProvider.id = :providerId")
    Optional<Service> findByIdAndProviderId(@Param("id") Long id, @Param("providerId") Long providerId);

    Page<Service> findAll(Specification<Service> spec, Pageable pageable);
    @Query("""
        SELECT s FROM Service s
        WHERE s.serviceProvider.id = :providerId
     """)
    List<Service> findAllServicesByProviderId(@Param("providerId") Long providerId);

    @Query("""
        SELECT new com.fpt.capstone.tourism.dto.response.PublicServiceDTO (
            s.id, s.name, s.sellingPrice, s.imageUrl, r.id, r.capacity, r.availableQuantity, r.facilities)
        FROM Service s
        JOIN Room r ON s.id = r.service.id
        WHERE s.serviceProvider.id = :providerId AND s.serviceCategory.categoryName = 'Hotel'
        AND s.deleted = FALSE 
     """)
    List<PublicServiceDTO> findRoomsByProviderId(@Param("providerId") Long id);

    @Query("""
        SELECT s FROM Service s
        WHERE s.serviceProvider.id = :providerId AND s.serviceCategory.categoryName != 'Hotel'
        AND s.deleted = FALSE 
     """)
    List<Service> findOtherServicesByProviderId(@Param("providerId")Long id);


//    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.serviceDetails WHERE s.id = :serviceId AND s.serviceProvider.id = :providerId")
//    Optional<Service> findByIdAndProviderIdWithServiceDetails(@Param("serviceId") Long serviceId, @Param("providerId") Long providerId);

    @Query("SELECT s FROM Service s WHERE s.id = :serviceId AND s.serviceProvider.id = :providerId")
    Optional<Service> findByIdAndServiceProviderId(@Param("serviceId") Long serviceId, @Param("providerId") Long providerId);

    List<Service> findByIdIn(List<Long> ids);

    boolean existsByNameAndServiceProviderId(String name, Long providerId);
    boolean existsByNameAndServiceProviderIdAndIdNot(String name, Long providerId, Long serviceId);

    List<Service> findByServiceProviderIdAndDeletedFalse(Long serviceProviderId);

    List<Service> findByServiceCategoryIdAndDeletedFalseOrderByIdDesc(Long categoryId);

    @Query("SELECT s FROM Service s " +
            "WHERE s.serviceProvider.id = :providerId " +
            "AND :locationId IN (SELECT l.id FROM Location l " +
            "JOIN ServiceProvider sp ON sp.location.id = l.id " +
            "WHERE sp.id = s.serviceProvider.id)")
    List<Service> findByServiceProviderIdAndLocationId(
            @Param("providerId") Long providerId,
            @Param("locationId") Long locationId);

    @Query("SELECT s FROM Service s " +
            "WHERE s.serviceProvider.id = :providerId " +
            "AND s.serviceCategory.id = :categoryId " +
            "AND (s.deleted = false OR s.deleted IS NULL)")
    List<Service> findByServiceProviderIdAndCategoryId(
            @Param("providerId") Long providerId,
            @Param("categoryId") Long categoryId);

    @Query("SELECT s FROM Service s " +
            "WHERE s.serviceCategory.id = :categoryId " +
            "AND :locationId IN (SELECT l.id FROM Location l " +
            "JOIN ServiceProvider sp ON sp.location.id = l.id " +
            "WHERE sp.id = s.serviceProvider.id)")
    List<Service> findByServiceCategoryIdAndLocationId(
            @Param("categoryId") Long categoryId,
            @Param("locationId") Long locationId);

    @Query("SELECT s FROM Service s " +
            "WHERE s.serviceCategory.categoryName = :categoryName " +
            "AND s.serviceProvider.id = :providerId " +
            "AND :locationId IN (SELECT l.id FROM Location l " +
            "JOIN ServiceProvider sp ON sp.location.id = l.id " +
            "WHERE sp.id = s.serviceProvider.id)")
    List<Service> findByServiceCategoryNameAndProviderIdAndLocationId(
            @Param("categoryName") String categoryName,
            @Param("providerId") Long providerId,
            @Param("locationId") Long locationId);

    @Query("""
    SELECT s, tbs.currentQuantity FROM Service s
    JOIN FETCH TourBookingService tbs ON s.id = tbs.service.id
    WHERE tbs.booking.tourSchedule.id = :scheduleId
    AND tbs.status NOT IN :tourBookingServiceStatusList
    AND s.serviceCategory.categoryName != 'Transport'
""")
    List<Object[]> findAllServicesWithQuantityInTourSchedule(Long scheduleId, List<TourBookingServiceStatus> tourBookingServiceStatusList);

    @Query("SELECT s FROM Service s " +
            "WHERE s.serviceCategory.categoryName = :categoryName " +
            "AND s.serviceProvider.id = :providerId ")
    List<Service> findByServiceCategoryNameAndProviderId(
            @Param("categoryName") String categoryName,
            @Param("providerId") Long providerId);

    @Query("""
            SELECT s FROM Service s 
            JOIN ServiceProvider sp ON s.serviceProvider.id = sp.id
            WHERE s.serviceCategory.categoryName = :categoryName 
            AND sp.location.id = :locationId 
            AND sp.deleted = FALSE 
            ORDER BY RANDOM()
            """)
    List<Service> findRelatedActivities(Long locationId, String categoryName, Pageable pageable);

    @Query("""
            SELECT s FROM Service s 
            WHERE s.serviceCategory.categoryName = :categoryName 
            AND s.deleted = FALSE 
            ORDER BY RANDOM()
            """)
    List<Service> findRandomActivities(String categoryName, PageRequest of);

    List<Service> findByServiceProviderIdAndServiceCategoryIdAndDeletedFalse(Long serviceProviderId, Long serviceCategoryId);

    @Query(value = """
            SELECT distinct s.nett_price as bigdecimal FROM service s
            JOIN public.tour_booking_service tbs ON s.id = tbs.service_id
            JOIN public.tour_booking tb ON tbs.tour_booking_id = tb.id
            WHERE tb.schedule_id = :scheduleId
            AND s.category_id = 3
            """, nativeQuery = true)
    List<BigDecimal> findTransportFeeByScheduleId(Long scheduleId);


    @Query(value = """
            SELECT sc.categoryName FROM Service s
            JOIN s.serviceCategory sc
            WHERE s.id = :id
            """)
    String findCategoryById(Long id);
}

