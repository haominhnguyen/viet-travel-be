package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    Optional<ServiceCategory> findByCategoryName(String categoryName);
    Page<ServiceCategory> findAll(Specification<ServiceCategory> spec, Pageable pageable);
    @Query("SELECT sc.categoryName FROM ServiceCategory sc WHERE sc.id IN :ids")
    List<String> findCategoryNamesByIds(@Param("ids") List<Long> ids);

    List<ServiceCategory> findByDeletedFalse();

    @Query("SELECT sc FROM ServiceCategory sc WHERE sc.deleted = false ORDER BY sc.categoryName")
    List<ServiceCategory> findAllActive();
}
