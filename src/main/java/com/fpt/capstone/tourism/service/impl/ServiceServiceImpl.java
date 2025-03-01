package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceBaseMapper;
import com.fpt.capstone.tourism.mapper.custom.ServiceCustomMapper;
import com.fpt.capstone.tourism.mapper.ServiceFullMapper;
import com.fpt.capstone.tourism.mapper.ServiceMapper;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceDetail;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.service.ServiceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    @PersistenceContext
    private EntityManager entityManager;

    private final ServiceRepository serviceRepository;
    private final ServiceBaseMapper serviceMapper;
    private final ServiceFullMapper serviceFullMapper;
    private final ServiceCustomMapper serviceCustomMapper;

    @Override
    public GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> getAllServices(
            int page, int size, String keyword, Boolean isDeleted, String sortField,
            String sortDirection, Long providerId) {
        try {
            // Validate sortField to prevent invalid field names
            List<String> allowedSortFields = Arrays.asList("id", "createdAt", "name");
            if (!allowedSortFields.contains(sortField)) {
                sortField = "createdAt";
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Service> spec = buildSearchSpecification(keyword, isDeleted, providerId);

            Page<Service> servicePage = serviceRepository.findAll(spec, pageable);
            List<ServiceBaseDTO> serviceDTOs = servicePage.getContent().stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(servicePage, serviceDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Failed to retrieve services", ex);
        }
    }

    private Specification<Service> buildSearchSpecification(String keyword, Boolean isDeleted, Long providerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by providerId
            predicates.add(cb.equal(root.get("serviceProvider").get("id"), providerId));

            // Search by name (ignoring accents and case)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));
                Expression<String> normalizedName = cb.function("unaccent", String.class, cb.lower(root.get("name")));

                Predicate namePredicate = cb.like(normalizedName, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                predicates.add(namePredicate);
            }

            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> buildPagedResponse(Page<Service> servicePage, List<ServiceBaseDTO> serviceDTOs) {
        PagingDTO<List<ServiceBaseDTO>> pagingDTO = PagingDTO.<List<ServiceBaseDTO>>builder()
                .page(servicePage.getNumber())
                .size(servicePage.getSize())
                .total(servicePage.getTotalElements())
                .items(serviceDTOs)
                .build();
        return GeneralResponse.of(pagingDTO, SERVICE_RETRIEVE_SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponse<ServiceFullDTO> getServiceById(Long id, Long providerId) {
        try {
            Service service = serviceRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            if (!service.getServiceProvider().getId().equals(providerId)) {
                throw BusinessException.of(HttpStatus.FORBIDDEN, SERVICE_NOT_BELONG_TO_PROVIDER);
            }
            System.out.println("Service Details: " + service.getServiceDetails().size());
            ServiceFullDTO serviceDTO = serviceCustomMapper.mapToServiceFullDTO(service);
            return GeneralResponse.of(serviceDTO, SERVICE_RETRIEVE_SUCCESS);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving service: " + e.getMessage());
        }
    }

}

