package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);

    @Query(value = """
SELECT * FROM tag t
         LEFT JOIN tour_tag tt ON t.id = tt.tag_id
WHERE tt.tour_id = :tourId
""", nativeQuery = true)
    List<Tag> findTagsByTourId(@Param("tourId")Long tourId);
}
