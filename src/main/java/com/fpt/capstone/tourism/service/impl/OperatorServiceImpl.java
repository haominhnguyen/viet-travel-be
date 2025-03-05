package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.OperatorTourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourPax;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import com.fpt.capstone.tourism.service.OperatorService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperatorServiceImpl implements OperatorService {
    private final TourScheduleRepository tourScheduleRepository;

    @Override
    public GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTour(int page, int size, String keyword, String status, String orderDate) {
        try {
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<TourSchedule> spec = buildSearchSpecification(keyword, status);

            Page<TourSchedule> tourPage = tourScheduleRepository.findAll(spec, pageable);


            List<Long> scheduleIds = tourPage.getContent().stream()
                    .map(TourSchedule::getId)
                    .collect(Collectors.toList());

            Map<Long, Integer> availableSeatsMap = tourScheduleRepository.findAvailableSeatsByScheduleIds(scheduleIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // scheduleId
                            row -> (Integer) row[1] // availableSeats
                    ));


            // Map to DTO
            List<OperatorTourDTO> operatorTourDTOS = tourPage.getContent().stream()
                    .map(tourSchedule -> new OperatorTourDTO(
                            tourSchedule.getId(),
                            tourSchedule.getStartDate(),
                            tourSchedule.getEndDate(),
                            tourSchedule.getStatus(),
                            tourSchedule.getTour().getName(),
                            Optional.ofNullable(tourSchedule.getTourGuide()).map(User::getFullName).orElse(null),
                            Optional.ofNullable(tourSchedule.getOperator()).map(User::getFullName).orElse(null),
                            tourSchedule.getTourPax().getMaxPax(),
                            availableSeatsMap.getOrDefault(tourSchedule.getId(), 0)
                    ))
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, operatorTourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Operator get all tour fail", ex);
        }
    }

    private Specification<TourSchedule> buildSearchSpecification(String keyword, String status) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            // Search by tour name
            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Ensure PostgreSQL has UNACCENT enabled
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.join("tour", JoinType.LEFT).get("name")));

                // Remove accents from the input keyword
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Combine both conditions
                predicates.add(tourNamePredicate);
            }


            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<TourSchedule> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }
}
