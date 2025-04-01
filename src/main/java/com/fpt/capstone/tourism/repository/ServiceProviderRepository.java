package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicServiceProviderDTO;
import com.fpt.capstone.tourism.model.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long>, JpaSpecificationExecutor<ServiceProvider> {
    ServiceProvider findByEmail(String email);

    ServiceProvider findByPhone(String phoneNumber);

    Optional<ServiceProvider> findByName(String serviceProviderName);

    boolean existsByName(String serviceProviderName);

    Optional<ServiceProvider> findByUserId(Long userId);

    @Query("""
            SELECT sv FROM ServiceProvider sv
            JOIN sv.location l
            JOIN sv.serviceCategories sc
            WHERE l.id = :locationId
            AND sc.categoryName = 'Hotel'
            AND sv.deleted = FALSE 
            ORDER BY RANDOM() LIMIT 6
                        """)
    List<ServiceProvider> getHotelByLocationId(@Param("locationId") Long id);


    @Query("""
            SELECT sv FROM ServiceProvider sv
            JOIN sv.location l
            JOIN sv.serviceCategories sc
            WHERE l.id = :locationId
            AND sc.categoryName = :categoryName
            AND sv.deleted = FALSE 
            ORDER BY RANDOM() LIMIT 6
                        """)
    List<ServiceProvider> getHotelByLocationIdAndServiceCategory(@Param("locationId") Long id, @Param("locationId") String categoryName);

    @Query("""
            SELECT s.serviceProvider.id, MIN(s.sellingPrice)
                FROM Service s
                WHERE s.serviceProvider.id IN :hotelIds
                GROUP BY s.serviceProvider.id

            """)
    List<Object[]> findMinRoomPricesByHotelIds(@Param("hotelIds") List<Long> hotelIds);

    List<ServiceProvider> findByLocationIdAndDeletedFalse(@Param("locationId") Long locationId);

    @Query("SELECT COUNT(sp) > 0 FROM ServiceProvider sp " +
            "JOIN sp.serviceCategories sc " +
            "WHERE sp.location.id = :locationId AND sc.categoryName = :categoryName " +
            "AND (sp.deleted = false OR sp.deleted IS NULL)")
    boolean existsByLocationIdAndCategoryName(@Param("locationId") Long locationId, @Param("categoryName") String categoryName);

    @Query("""
                SELECT sp FROM ServiceProvider sp 
                JOIN sp.serviceCategories sc 
                WHERE sp.location.id = :locationId 
                AND sc.id = :serviceCategoryId 
                AND sp.deleted = false
            """)
    List<ServiceProvider> findByLocationIdAndServiceCategoryIdAndDeletedFalse(Long locationId, Long serviceCategoryId);
    @Query("SELECT sp FROM ServiceProvider sp " +
            "JOIN sp.serviceCategories sc " +
            "WHERE sp.location.id = :locationId " +
            "AND sc.id = :categoryId " +
            "AND (sp.deleted = false OR sp.deleted IS NULL)")
    List<ServiceProvider> findByLocationIdAndServiceCategoryId(
            @Param("locationId") Long locationId,
            @Param("categoryId") Long categoryId);

    @Query("SELECT sp FROM ServiceProvider sp " +
            "WHERE sp.location.id = :locationId " +
            "AND (sp.deleted = false OR sp.deleted IS NULL)")
    List<ServiceProvider> findByLocationIdAndNotDeleted(@Param("locationId") Long locationId);

    @Query("SELECT DISTINCT sc.categoryName FROM ServiceProvider sp " +
            "JOIN sp.serviceCategories sc " +
            "WHERE sp.location.id = :locationId " +
            "AND (sp.deleted = false OR sp.deleted IS NULL)")
    List<String> findAvailableCategoriesByLocationId(@Param("locationId") Long locationId);


    @Query("""
            SELECT sp FROM ServiceProvider sp
            JOIN sp.serviceCategories sc
            WHERE sp.location.id = :locationId
            AND sp.id!= :serviceProviderId
            AND sp.deleted = false
            AND sc.categoryName = 'Hotel'
            ORDER BY RANDOM() LIMIT 6
""")
    List<ServiceProvider> findOtherHotelsInSameLocationByProviderId(Long serviceProviderId, Long locationId);



    @Query("SELECT DISTINCT s.serviceProvider FROM TourBookingService tbs " +
            "JOIN tbs.service s " +
            "WHERE tbs.booking.id = :bookingId AND tbs.deleted = false")
    List<ServiceProvider> findDistinctServiceProvidersByBookingId(@Param("bookingId") Long bookingId);

}

