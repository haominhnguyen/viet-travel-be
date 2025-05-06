package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServicePaxPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePaxPricingRepository extends JpaRepository<ServicePaxPricing, Long> {
    List<ServicePaxPricing> findByTourDayServiceIdIn(List<Long> tourDayServiceIds);
    List<ServicePaxPricing> findByTourDayServiceId(Long tourDayServiceId);
    ServicePaxPricing findByTourDayServiceIdAndTourPaxId(Long tourDayServiceId, Long tourPaxId);
    List<ServicePaxPricing> findByTourPaxId(Long tourPaxId);
    List<ServicePaxPricing> findByTourPaxIdIn(List<Long> tourPaxIds);
    void deleteByTourPaxId(Long tourPaxId);

    List<ServicePaxPricing> findByTourPaxIdAndDeletedFalse(Long paxId);

    List<ServicePaxPricing> findByTourPaxIdInAndDeletedFalse(List<Long> paxIds);

    @Query("SELECT spp FROM ServicePaxPricing spp WHERE spp.tourDayService.id IN :tourDayServiceIds AND spp.deleted = false")
    List<ServicePaxPricing> findByTourDayServiceIdInAndDeletedFalse(@Param("tourDayServiceIds") List<Long> tourDayServiceIds);

    List<ServicePaxPricing> findByTourDayServiceIdAndDeletedFalse(Long id);
    @Query(value = """
            SELECT spp.selling_price FROM service_pax_pricing spp
            WHERE spp.tour_day_service_id = :id 
            AND spp.tour_pax_id = :tourPaxId
            """, nativeQuery = true)
    Double findSellingPriceByTourDayServiceIdAndTourPaxId(Long id, int tourPaxId);

    @Query(value = """
           SELECT spp FROM ServicePaxPricing spp
           WHERE spp.tourPax.id = :paxId
           AND spp.tourDayService.id IN :tourDayServiceIds
            """)
    List<ServicePaxPricing> findByTourDayServiceIdInAndTourPaxId(List<Long> tourDayServiceIds, int paxId);
}
