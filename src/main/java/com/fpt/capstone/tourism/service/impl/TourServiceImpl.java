package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBasicDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.TourMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.service.TourService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    public TourDTO findTopTourOfYear() {
        try{
            List<Long> topTourIds = tourRepository.findTopTourIdsOfCurrentYear();

            if (topTourIds.isEmpty()) {
                Tour tempTour = tourRepository.findNewestTour();
                return tourMapper.toDTO(tempTour);
                //throw BusinessException.of("Top tour of the year not found");
            }

            // Pick a random tour ID from the list
//            Long randomTourId = topTourIds.get(new Random().nextInt(topTourIds.size()));
            Long topTourId = topTourIds.get(0);

            // Fetch and convert the tour to DTO
            return tourRepository.findById(topTourId)
                    .map(tourMapper::toDTO).orElseThrow();
        } catch (Exception ex){
            throw BusinessException.of("Error retrieving top tour of year", ex);
        }

    }

    @Override
    public List<TourDTO> findTrendingTours(int numberTour) {
        Pageable pageable = PageRequest.of(0, numberTour);
        List<Long> trendingTourIds = tourRepository.findTrendingTourIds(pageable);

        // Fetch all tours by their IDs and convert to DTOs
        return tourRepository.findAllById(trendingTourIds)
                .stream()
                .map(tourMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<Tour> spec = buildSearchSpecification(keyword, budgetFrom, budgetTo, duration, fromDate);

            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);
            List<TourDTO> tourDTOS = tourPage.getContent().stream()
                    .map(tourMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, tourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("not ok", ex);
        }
    }

    private GeneralResponse<PagingDTO<List<TourDTO>>> buildPagedResponse(Page<Tour> tourPage, List<TourDTO> tours) {
        PagingDTO<List<TourDTO>> pagingDTO = PagingDTO.<List<TourDTO>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }

    private Specification<Tour> buildSearchSpecification(String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted tours
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("open"), true));

            // Search by tour name OR depart location name
            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Ensure PostgreSQL has UNACCENT enabled
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Expression<String> normalizedLocationName = cb.function("unaccent", String.class, cb.lower(root.join("locations", JoinType.LEFT).get("name")));

                // Remove accents from the input keyword
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                Predicate locationNamePredicate = cb.like(normalizedLocationName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Combine both conditions
                predicates.add(cb.or(tourNamePredicate, locationNamePredicate));
            }



            // Filter by duration (number of days)
            if (duration != null && duration > 0) {
                predicates.add(cb.equal(root.get("numberDays"), duration));
            }

            // Filter by tour schedule date
            if (fromDate != null) {
                Join<Tour, TourSchedule> scheduleJoin = root.join("tourSchedules", JoinType.LEFT);
                predicates.add(cb.greaterThanOrEqualTo(scheduleJoin.get("date"), fromDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
