package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceFullMapper;
import com.fpt.capstone.tourism.mapper.ServiceMapper;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.service.ServiceService;
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
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ServiceFullMapper serviceFullMapper;

    @Override
    public GeneralResponse<PagingDTO<List<ServiceDTO>>> getAllServices(
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
            Specification<com.fpt.capstone.tourism.model.Service> spec = buildSearchSpecification(keyword, isDeleted, providerId);

            Page<com.fpt.capstone.tourism.model.Service> servicePage = serviceRepository.findAll(spec, pageable);
            List<ServiceDTO> serviceDTOs = servicePage.getContent().stream()
                    .map(serviceMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(servicePage, serviceDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Failed to retrieve services", ex);
        }
    }

    private Specification<com.fpt.capstone.tourism.model.Service> buildSearchSpecification(String keyword, Boolean isDeleted, Long providerId) {
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

    private GeneralResponse<PagingDTO<List<ServiceDTO>>> buildPagedResponse(Page<com.fpt.capstone.tourism.model.Service> servicePage, List<ServiceDTO> serviceDTOs) {
        PagingDTO<List<ServiceDTO>> pagingDTO = PagingDTO.<List<ServiceDTO>>builder()
                .page(servicePage.getNumber())
                .size(servicePage.getSize())
                .total(servicePage.getTotalElements())
                .items(serviceDTOs)
                .build();
        return GeneralResponse.of(pagingDTO, SERVICE_RETRIEVE_SUCCESS);
    }

    @Override
    public GeneralResponse<ServiceFullDTO> getServiceById(Long id, Long providerId) {
        com.fpt.capstone.tourism.model.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));
        try{
            if (!service.getServiceProvider().getId().equals(providerId)) {
                throw BusinessException.of(HttpStatus.FORBIDDEN, SERVICE_NOT_BELONG_TO_PROVIDER);
            }
            ServiceFullDTO serviceDTO = serviceFullMapper.toDTO(service);
            return GeneralResponse.of(serviceDTO, SERVICE_RETRIEVE_SUCCESS);
        } catch (Exception e) {
            throw BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND);
        }

    }

}

