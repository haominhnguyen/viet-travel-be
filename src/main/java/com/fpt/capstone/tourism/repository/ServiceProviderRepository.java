package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long>, JpaSpecificationExecutor<ServiceProvider> {
    ServiceProvider findByEmail(String email);
    ServiceProvider findByPhone(String phoneNumber);
    Optional<ServiceProvider> findByName(String serviceProviderName);
    boolean existsByName(String serviceProviderName);
    Optional<ServiceProvider> findByUserId(Long userId);

}

