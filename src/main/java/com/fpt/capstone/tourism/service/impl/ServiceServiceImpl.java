package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.service.ServiceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
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

    private final ServiceRepository serviceRepository;
    private final ServiceBaseMapper serviceBaseMapper;
    private final ServiceDetailMapper serviceDetailMapper;
    private final TourDayServiceMapper tourDayServiceMapper;

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
                    .map(serviceBaseMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(servicePage, serviceDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Failed to retrieve services", ex);
        }
    }

    public GeneralResponse<List<TourDayServiceDTO>> getTourDayServicesByServiceId(Long serviceId, Long providerId) {
        try{
            Service service = serviceRepository.findByIdAndProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            List<TourDayServiceDTO> tourDayServices = service.getTourDayServices()
                    .stream()
                    .map(tourDayServiceMapper::toDTO)
                    .collect(Collectors.toList());

            return GeneralResponse.of(tourDayServices, TOUR_DAY_SERVICES_RETRIEVED);
        } catch (BusinessException e) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, GET_TOUR_DAY_SERVICE_FAIL);
        }
    }

    public GeneralResponse<List<ServiceDetailDTO>> getServiceDetailsByServiceId(Long serviceId, Long providerId) {
        try {
            // Use a query that specifically fetches the service details to avoid the circular reference issue
            Service service = serviceRepository.findByIdAndProviderIdWithServiceDetails(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            List<ServiceDetailDTO> serviceDetails = service.getServiceDetails()
                    .stream()
                    .map(serviceDetailMapper::toDTO)
                    .collect(Collectors.toList());

            return GeneralResponse.of(serviceDetails, SERVICE_DETAILS_RETRIEVED);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, GET_SERVICE_DETAIL_FAIL);
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

}

