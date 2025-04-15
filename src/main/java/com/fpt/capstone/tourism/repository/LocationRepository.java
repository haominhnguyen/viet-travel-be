package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.PublicLocationSimpleDTO;
import com.fpt.capstone.tourism.dto.common.PublicLocationSimpleProviderDTO;
import com.fpt.capstone.tourism.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Location findByName(String name);
    Location findByNameLike(String name);

    @Query(value = "SELECT * FROM location WHERE is_deleted = FALSE ORDER BY RANDOM() LIMIT :numberLocation", nativeQuery = true)
    List<Location> findRandomLocation(@Param("numberLocation") int numberLocation);

    @Query(value = "SELECT * FROM location WHERE is_deleted = FALSE AND location.id != :locationId ORDER BY RANDOM() LIMIT :numberLocation", nativeQuery = true)
    List<Location> findRandomLocation(@Param("numberLocation") int numberLocation, @Param("locationId") Long locationId);

    List<Location> findByDeletedFalse();

    @Query("""
            SELECT new com.fpt.capstone.tourism.dto.common.PublicLocationSimpleProviderDTO(
            l.id,
            l.name,
            gp.id, gp.latitude, gp.longitude
            )
                    FROM Location l 
                    LEFT JOIN GeoPosition gp ON l.geoPosition.id = gp.id
                    WHERE l.deleted = FALSE
            """)
    List<PublicLocationSimpleProviderDTO> findLocationSimple();

    @Query("""
            SELECT l
            FROM Location l
            JOIN l.tours t
            JOIN TourSchedule ts ON t.id = ts.tour.id
            WHERE l.deleted = FALSE AND ts.id = :tourScheduleId
            """)
    List<Location> findLocationOfTourByScheduleId(Long tourScheduleId);
}
