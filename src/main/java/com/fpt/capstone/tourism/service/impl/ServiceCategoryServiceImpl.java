package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceCategoryMapper;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.repository.ServiceCategoryRepository;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;

    @Override
    public GeneralResponse<ServiceCategoryDTO> createCategory(ServiceCategoryDTO requestDTO) {
        if (serviceCategoryRepository.findByCategoryName(requestDTO.getName()).isPresent()) {
            throw BusinessException.of(HttpStatus.CONFLICT, CATEGORY_ALREADY_EXISTS);
        }
        ServiceCategory serviceCategory = serviceCategoryMapper.toEntity(requestDTO);
        serviceCategory.setDeleted(false);
        ServiceCategory savedCategory = serviceCategoryRepository.save(serviceCategory);
        return GeneralResponse.of(serviceCategoryMapper.toDTO(savedCategory), CATEGORY_CREATED);
    }

    @Override
    public GeneralResponse<ServiceCategoryDTO> getCategoryById(Long id) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));

        return GeneralResponse.of(serviceCategoryMapper.toDTO(category), CATEGORY_LOADED);
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceCategoryDTO>>> getAllCategories(
            int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection) {
        try {
            // Validate sortField to prevent invalid field names
            List<String> allowedSortFields = Arrays.asList("id", "createdAt", "categoryName");
            if (!allowedSortFields.contains(sortField)) {
                sortField = "createdAt";
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<ServiceCategory> spec = buildSearchSpecification(keyword, isDeleted);

            Page<ServiceCategory> categoryPage = serviceCategoryRepository.findAll(spec, pageable);
            List<ServiceCategoryDTO> categories = categoryPage.getContent().stream()
                    .map(serviceCategoryMapper::toDTO)
                    .collect(Collectors.toList());

            PagingDTO<List<ServiceCategoryDTO>> pagingDTO = PagingDTO.<List<ServiceCategoryDTO>>builder()
                    .page(page)
                    .size(size)
                    .total(categoryPage.getTotalElements())
                    .items(categories)
                    .build();

            return GeneralResponse.of(pagingDTO, CATEGORY_LOADED);
        } catch (Exception e) {
            throw BusinessException.of("Failed to fetch service categories", e);
        }
    }


    private Specification<ServiceCategory> buildSearchSpecification(String keyword, Boolean isDeleted) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedCategoryName = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.lower(root.get("categoryName")));
                Expression<String> normalizedKeyword = criteriaBuilder.function("unaccent", String.class, criteriaBuilder.literal(keyword.toLowerCase()));

                Predicate keywordPredicate = criteriaBuilder.like(normalizedCategoryName, criteriaBuilder.concat("%", criteriaBuilder.concat(normalizedKeyword, "%")));
                predicates.add(keywordPredicate);
            }
            if (isDeleted != null) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), isDeleted));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    @Override
    public GeneralResponse<ServiceCategoryDTO> updateCategory(Long id, ServiceCategoryDTO requestDTO) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));

        Optional<ServiceCategory> existingCategory = serviceCategoryRepository.findByCategoryName(requestDTO.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw BusinessException.of(HttpStatus.CONFLICT, CATEGORY_ALREADY_EXISTS);
        }

        category.setCategoryName(requestDTO.getName());
        ServiceCategory updatedCategory = serviceCategoryRepository.save(category);

        return GeneralResponse.of(serviceCategoryMapper.toDTO(updatedCategory), CATEGORY_UPDATED);
    }

    @Override
    public GeneralResponse<ServiceCategoryDTO> changeCategoryDeletedStatus(Long id, boolean isDeleted) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));
        category.setDeleted(isDeleted);
        ServiceCategory savedCategory = serviceCategoryRepository.save(category);
        return GeneralResponse.of(serviceCategoryMapper.toDTO(savedCategory), CATEGORY_UPDATED);
    }
}

