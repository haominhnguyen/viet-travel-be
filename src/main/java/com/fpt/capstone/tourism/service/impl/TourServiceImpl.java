package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TagRepository;
import com.fpt.capstone.tourism.repository.TourImageRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;
    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourImageRepository tourImageRepository;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public PublicTourDTO findTopTourOfYear() {
        try{
            List<Long> topTourIds = tourRepository.findTopTourIdsOfCurrentYear();

            if (topTourIds.isEmpty()) {
                Tour tempTour = tourRepository.findNewestTour();
                return PublicTourDTO.builder()
                        .id(tempTour.getId())
                        .name(tempTour.getName())
                        .numberNight(tempTour.getNumberNight())
                        .numberDays(tempTour.getNumberDays())
                        .tags(tempTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                        .depart_location(locationMapper.toPublicLocationDTO(tempTour.getDepart_location()))
                        .tourImages(tempTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                        .priceFrom(tourRepository.findMinSellingPriceForTours(tempTour.getId()))
                        .build();
            }

            Long topTourId = topTourIds.get(0);

            // Fetch and convert the tour to DTO
            Tour topTour = tourRepository.findById(topTourIds.get(0)).orElseThrow();
            return PublicTourDTO.builder()
                    .id(topTour.getId())
                    .name(topTour.getName())
                    .numberNight(topTour.getNumberNight())
                    .numberDays(topTour.getNumberDays())
                    .tags(topTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .depart_location(locationMapper.toPublicLocationDTO(topTour.getDepart_location()))
                    .tourImages(topTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                    .priceFrom(tourRepository.findMinSellingPriceForTours(topTour.getId()))
                    .build();
        } catch (Exception ex){
            throw BusinessException.of("Error retrieving top tour of year", ex);
        }

    }

    @Override
    public List<PublicTourDTO> findTrendingTours(int numberTour) {
        try{
            Pageable pageable = PageRequest.of(0, numberTour);
            List<Long> trendingTourIds = tourRepository.findTrendingTourIds(pageable);

            // Lấy danh sách các tour từ database theo danh sách ID
            List<Tour> trendingTours = tourRepository.findAllById(trendingTourIds);

            // Lấy giá thấp nhất từ bảng TourPax
            Map<Long, Double> priceMap = tourRepository.findMinSellingPrices(trendingTourIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // tourId
                            row -> (Double) row[1] // priceFrom
                    ));

            // Fetch all tours by their IDs and convert to DTOs
            return trendingTours.stream()
                    .map(tour -> new PublicTourDTO(
                            tour.getId(),
                            tour.getName(),
                            tour.getNumberDays(),
                            tour.getNumberNight(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),  // Convert tags
                            locationMapper.toPublicLocationDTO(tour.getDepart_location()),  // Convert depart location
                            tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).toList(), // Convert images
                            priceMap.getOrDefault(tour.getId(), 0.0) // Giá thấp nhất
                    ))
                    .collect(Collectors.toList());
        }catch (Exception ex){
            throw BusinessException.of("Error retrieving trending tours", ex);
        }

    }

    @Override
    public GeneralResponse<PagingDTO<List<PublicTourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<Tour> spec = buildSearchSpecification(keyword, budgetFrom, budgetTo, duration, fromDate);

            //Find TOur satisfying conditions
            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

            //Find min price of each tour
            Map<Long, Double> minPriceMap = tourRepository.findMinSellingPrices(tourPage.getContent().stream().map(Tour::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // tourId
                            row -> (Double) row[1] // priceFrom
                    ));;

            List<PublicTourDTO> publicTourDTOS = tourPage.getContent().stream()
                    .map(tour -> new PublicTourDTO(
                            tour.getId(),
                            tour.getName(),
                            tour.getNumberDays(),
                            tour.getNumberNight(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),
                            locationMapper.toPublicLocationDTO(tour.getDepart_location()),
                            tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).toList(),
                            minPriceMap.getOrDefault(tour.getId(), 0.0)  // Giá thấp nhất
                    ))
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, publicTourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Get all public tour fail", ex);
        }
    }

    @Override
    public List<PublicTourDTO> findSameLocationPublicTour(List<Long> locationIds) {
        try {
            List<PublicTourDTO> publicTourDTOS = new ArrayList<>();
            //Get list id of list same location tour
            List<Long> tourIds = tourRepository.findSameLocationTourIds(locationIds);
            for(Long tourId : tourIds) {
                //Get tour information
                Tour tour = tourRepository.findById(tourId).orElseThrow();

                //Get list tag for each tour
                List<TagDTO> tags = tagRepository.findTagsByTourId(tourId)
                        .stream().map(tagMapper::toDTO).toList();;

                //Get min price for each tour
                Double minPrice = tourRepository.findMinSellingPriceForTours(tourId);

                // Get list image for each tour
                List<PublicTourImageDTO> images = tourImageRepository.findTourImagesByTourId(tourId)
                        .stream().map(tourImageMapper::toPublicTourImageDTO).toList();;


                PublicTourDTO publicTourDTO = PublicTourDTO.builder()
                        .id(tourId)
                        .name(tour.getName())
                        .numberDays(tour.getNumberDays())
                        .numberNight(tour.getNumberNight())
                        .tags(tags)
                        .depart_location(locationMapper.toPublicLocationDTO(tour.getDepart_location()))
                        .tourImages(images)
                        .priceFrom(minPrice)
                        .build();

                publicTourDTOS.add(publicTourDTO);
            }
            return publicTourDTOS;
        } catch (Exception ex){
            throw BusinessException.of("Error retrieving same location public tours", ex);
        }
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<Tour> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }

    private Specification<Tour> buildSearchSpecification(String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted tours
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("opened"), true));

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
                predicates.add(cb.greaterThanOrEqualTo(scheduleJoin.get("startDate"), fromDate));
            }

            //Filter by price of tour
            if(budgetFrom != null) {
                Join<Tour, TourPax> paxJoin = root.join("tourPax", JoinType.LEFT);
                predicates.add(cb.greaterThanOrEqualTo(paxJoin.get("sellingPrice"), budgetFrom));
            }

            if(budgetTo!= null) {
                Join<Tour, TourPax> paxJoin = root.join("tourPax", JoinType.LEFT);
                predicates.add(cb.lessThanOrEqualTo(paxJoin.get("sellingPrice"), budgetTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
