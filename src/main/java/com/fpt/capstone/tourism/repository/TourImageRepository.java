package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.TourImageDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage, Long>, JpaSpecificationExecutor<TourImage> {
    @Query("""
SELECT ti FROM TourImage ti WHERE ti.tour.id = :tourId 
""")
    List<TourImage> findTourImagesByTourId(@Param("tourId")Long tourId);

    @Query(value = """
    SELECT DISTINCT ON (tour_id) tour_id, image_url
    FROM tour_image
    WHERE tour_id IN (:tourIds)
    ORDER BY tour_id, id
""", nativeQuery = true)
    List<Object[]> findFirstImageForTours(List<Long> tourIds);

    @Query(value = """
    SELECT tour_id, image_url 
    FROM tour_image 
    WHERE tour_id IN (:ids)
    ORDER BY tour_id, id
""", nativeQuery = true)
    List<Object[]> findImagesByTourIds(@Param("ids") List<Long> ids);

}
