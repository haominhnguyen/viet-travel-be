package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.response.PublicServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceDetailDTO;
import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
        SELECT s FROM Service s
        WHERE s.serviceProvider.id = :providerId AND s.serviceCategory.categoryName = 'Hotel'
        AND s.deleted = FALSE 
     """)
    List<Service> findRoomsByProviderId(@Param("providerId") Long id);

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
            @Param("locationId") Long locationId);}

