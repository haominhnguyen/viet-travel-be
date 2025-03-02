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

    @Query("SELECT DISTINCT s FROM Service s " +
            "LEFT JOIN FETCH s.serviceCategory " +
            "LEFT JOIN FETCH s.serviceProvider " +
            //"LEFT JOIN FETCH s.serviceDetails " +
            "WHERE s.id = :serviceId")
    Optional<Service> findByIdWithDetails(@Param("serviceId") Long serviceId);

    @Query("SELECT DISTINCT s FROM Service s " +
            "LEFT JOIN FETCH s.serviceDetails " +
            "LEFT JOIN FETCH s.tourDayServices tds " +
            "LEFT JOIN FETCH tds.tourDay " +
            "WHERE s.id = :serviceId")
    Optional<Service> findByIdWithTourDayServiceAndDetails(@Param("serviceId") Long serviceId);



}

