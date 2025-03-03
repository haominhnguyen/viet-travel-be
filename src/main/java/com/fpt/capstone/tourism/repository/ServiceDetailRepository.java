package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceDetailRepository extends JpaRepository<ServiceDetail, Long> {
    List<ServiceDetail> findAllByServiceId(Long serviceId);
    Optional<ServiceDetail> findByIdAndServiceId(Long id, Long serviceId);
    boolean existsByServiceIdAndTitleIgnoreCase(Long serviceId, String title);
    Optional<ServiceDetail> findByServiceIdAndTitleIgnoreCase(Long serviceId, String title);
}
