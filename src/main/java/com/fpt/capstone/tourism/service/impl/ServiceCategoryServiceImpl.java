package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryFullDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.dto.request.ServiceCategoryRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceCategoryResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceCategoryDetailMapper;
import com.fpt.capstone.tourism.mapper.ServiceCategoryFullMapper;
import com.fpt.capstone.tourism.mapper.ServiceCategoryMapper;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.repository.ServiceCategoryRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryFullMapper serviceCategoryFullMapper;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCategoryDetailMapper serviceCategoryDetailMapper;

    @Override
    public GeneralResponse<ServiceCategoryFullDTO> getCategoryById(Long id) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));

        // Convert to DTO including simplified service providers
        ServiceCategoryFullDTO dto = serviceCategoryFullMapper.toDTO(category);
        dto.setServiceProviders(serviceCategoryFullMapper.toSimpleProviderDTOList(category.getServiceProviders()));

        return GeneralResponse.of(dto, CATEGORY_LOADED);
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceCategoryFullDTO>>> getAllCategories(
            int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection) {
        try {
            // Validate sortField
            List<String> allowedSortFields = Arrays.asList("id", "createdAt", "categoryName");
            if (!allowedSortFields.contains(sortField)) {
                sortField = "createdAt";
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<ServiceCategory> spec = buildSearchSpecification(keyword, isDeleted);

            Page<ServiceCategory> categoryPage = serviceCategoryRepository.findAll(spec, pageable);
            List<ServiceCategoryFullDTO> categories = categoryPage.getContent().stream()
                    .map(serviceCategory -> {
                        ServiceCategoryFullDTO dto = serviceCategoryFullMapper.toDTO(serviceCategory);
                        dto.setServiceProviders(serviceCategoryFullMapper.toSimpleProviderDTOList(serviceCategory.getServiceProviders()));
                        return dto;
                    })
                    .collect(Collectors.toList());

            PagingDTO<List<ServiceCategoryFullDTO>> pagingDTO = PagingDTO.<List<ServiceCategoryFullDTO>>builder()
                    .page(page)
                    .size(size)
                    .total(categoryPage.getTotalElements())
                    .items(categories)
                    .build();

            return GeneralResponse.of(pagingDTO, CATEGORY_LOADED);
        } catch (Exception e) {
            throw BusinessException.of("Tải danh mục thất bại", e);
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
    public GeneralResponse<ServiceCategoryResponseDTO> createCategory(ServiceCategoryRequestDTO requestDTO) {
        if (serviceCategoryRepository.findByCategoryName(requestDTO.getCategoryName()).isPresent()) {
            throw BusinessException.of(HttpStatus.CONFLICT, CATEGORY_ALREADY_EXISTS);
        }
        ServiceCategory serviceCategory = serviceCategoryDetailMapper.toEntity(requestDTO);
        serviceCategory.setDeleted(false);

        ServiceCategory savedCategory = serviceCategoryRepository.save(serviceCategory);
        return GeneralResponse.of(serviceCategoryDetailMapper.toDTO(savedCategory), CATEGORY_CREATED);
    }

    @Override
    public GeneralResponse<ServiceCategoryResponseDTO> updateCategory(Long id, ServiceCategoryRequestDTO requestDTO) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));

        Optional<ServiceCategory> existingCategory = serviceCategoryRepository.findByCategoryName(requestDTO.getCategoryName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw BusinessException.of(HttpStatus.CONFLICT, CATEGORY_ALREADY_EXISTS);
        }

        category.setCategoryName(requestDTO.getCategoryName());
        ServiceCategory updatedCategory = serviceCategoryRepository.save(category);

        return GeneralResponse.of(serviceCategoryDetailMapper.toDTO(updatedCategory), CATEGORY_UPDATED);
    }

    @Override
    public GeneralResponse<ServiceCategoryDTO> changeCategoryDeletedStatus(Long id, boolean isDeleted) {
        ServiceCategory category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND));
        category.setDeleted(isDeleted);
        ServiceCategory savedCategory = serviceCategoryRepository.save(category);
        return GeneralResponse.of(serviceCategoryMapper.toDTO(savedCategory), CATEGORY_UPDATED);
    }

    @Override
    public GeneralResponse<List<ServiceCategoryDTO>> getAllServiceCategories() {
        try {
            List<ServiceCategory> categories = serviceCategoryRepository.findAllActive();

            List<ServiceCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), CATEGORIES_LOAD_SUCCESS, categoryDTOs);
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, CATEGORIES_LOAD_FAIL, ex);
        }
    }

    private ServiceCategoryDTO mapToDTO(ServiceCategory category) {
        return ServiceCategoryDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .build();
    }
}


