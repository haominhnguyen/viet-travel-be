package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransportRepository extends JpaRepository<Transport, Long> {
    Optional<Transport> findByServiceId(Long serviceId);
    Optional<Transport> findByServiceIdAndDeletedFalse(Long serviceId);
}
