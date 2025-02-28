package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    @Query("SELECT s FROM Service s WHERE s.id = :id AND s.serviceProvider.id = :providerId")
    Optional<Service> findByIdAndProviderId(@Param("id") Long id, @Param("providerId") Long providerId);

    Page<Service> findAll(Specification<Service> spec, Pageable pageable);
}

