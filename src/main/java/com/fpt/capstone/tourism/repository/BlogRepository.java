package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.BlogDTO;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {
    @Query("SELECT b FROM Blog b WHERE b.deleted = FALSE ORDER BY b.createdAt DESC")
    List<Blog> findTopBlogs(Pageable pageable);

    @Query("SELECT b FROM Blog b JOIN b.blogTags t WHERE t.name = :tagName")
    List<Blog> findByBlogTags_Name(@Param("tagName") String tagName, Pageable pageable);

    @Query(value = "SELECT * FROM blog WHERE LOWER(UNACCENT(title)) LIKE LOWER(UNACCENT(CONCAT('%', :keyword, '%')))" +
            "AND blog.is_deleted = false", nativeQuery = true)
    List<Blog> findBlogRelatedLocations(@Param("keyword") String location);
}
