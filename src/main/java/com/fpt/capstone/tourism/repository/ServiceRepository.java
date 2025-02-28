package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository  extends JpaRepository<Service, Long> {

    @Query("""
        SELECT s FROM Service s
        WHERE s.serviceProvider.id = :providerId
     """)
    List<Service> findAllServicesByProviderId(@Param("providerId") Long providerId);
}
