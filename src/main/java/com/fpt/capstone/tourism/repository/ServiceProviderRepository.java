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
SELECT s.serviceProvider.id, MIN(s.sellingPrice)
    FROM Service s
    WHERE s.serviceProvider.id IN :hotelIds
    GROUP BY s.serviceProvider.id

""")
    List<Object[]> findMinRoomPricesByHotelIds(@Param("hotelIds") List<Long> hotelIds);

    List<ServiceProvider> findByLocationIdAndDeletedFalse(@Param("locationId") Long locationId);
}

