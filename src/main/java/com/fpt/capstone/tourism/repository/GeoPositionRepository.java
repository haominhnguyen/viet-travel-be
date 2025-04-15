package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.GeoPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeoPositionRepository extends JpaRepository<GeoPosition, Long> {
    List<GeoPosition> findByLatitudeAndLongitude(Double newLatitude, Double newLongitude);
}
