package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServicePaxPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePaxPricingRepository extends JpaRepository<ServicePaxPricing, Long> {
    List<ServicePaxPricing> findByTourDayServiceIdIn(List<Long> tourDayServiceIds);

    List<ServicePaxPricing> findByTourDayServiceId(Long tourDayServiceId);

    ServicePaxPricing findByTourDayServiceIdAndTourPaxId(Long tourDayServiceId, Long tourPaxId);
}
