package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourSimpleDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TagRepository;
import com.fpt.capstone.tourism.repository.TourImageRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@RequiredArgsConstructor
@Service
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourMapper tourMapper;
    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourImageRepository tourImageRepository;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final TourDayMapper tourDayMapper;

    @Override
    public PublicTourDTO findTopTourOfYear() {
        try{
            List<Long> topTourIds = tourRepository.findTopTourIdsOfCurrentYear();

            if (topTourIds.isEmpty()) {
                Tour tempTour = tourRepository.findNewestTour();
                return PublicTourDTO.builder()
                        .id(tempTour.getId())
                        .name(tempTour.getName())
                        .numberNight(tempTour.getNumberNights())
                        .numberDays(tempTour.getNumberDays())
                        .tags(tempTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                        .departLocation(locationMapper.toPublicLocationDTO(tempTour.getDepartLocation()))
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
                    .numberNight(topTour.getNumberNights())
                    .numberDays(topTour.getNumberDays())
                    .tags(topTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(topTour.getDepartLocation()))
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
                            tour.getNumberNights(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),  // Convert tags
                            locationMapper.toPublicLocationDTO(tour.getDepartLocation()),  // Convert depart location
                            tourScheduleRepository.findTourScheduleBasicByTourId(tour.getId()),
                            tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).toList(), // Convert images
                            priceMap.getOrDefault(tour.getId(), 0.0) // Giá thấp nhất
                    ))
                    .collect(Collectors.toList());
        }catch (Exception ex){
            throw BusinessException.of("Error retrieving trending tours", ex);
        }

    }

    @Override
    public GeneralResponse<PagingDTO<List<PublicTourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<Tour> spec = buildSearchSpecification(keyword, budgetFrom, budgetTo, duration, fromDate, departLocationId);

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
                            tour.getNumberNights(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),
                            locationMapper.toPublicLocationDTO(tour.getDepartLocation()),
                            tourScheduleRepository.findTourScheduleBasicByTourId(tour.getId()),
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
                        .numberNight(tour.getNumberNights())
                        .tags(tags)
                        .departLocation(locationMapper.toPublicLocationDTO(tour.getDepartLocation()))
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

    @Override
    public GeneralResponse<PagingDTO<List<TourSimpleDTO>>> getAllTours(String keyword, Boolean isDeleted, Boolean isOpened,Pageable pageable) {
        Specification<Tour> spec = buildSimpleSearchSpecification(keyword, isDeleted, isOpened);
        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);
        List<TourSimpleDTO> tourDTOs = tourPage.getContent().stream()
                .map(this::convertToTourSimpleDTO)
                .collect(Collectors.toList());
        return buildSimplePagedResponse(tourPage, tourDTOs);
    }

    @Override
    public GeneralResponse<TourDetailDTO> getTourDetail(Long id) {
        try{
            Tour currentTour = tourRepository.findById(id).orElseThrow();
            List<Long> locationIds = currentTour.getLocations().stream().map(location -> location.getId()).collect(Collectors.toList());
            List<PublicTourScheduleDTO> tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleBasicByTourId(id);

            //Mapping to DTO
            TourDetailDTO tourBasicDTO = TourDetailDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .highlights(currentTour.getHighlights())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNights())
                    .note(currentTour.getNote())
                    .privacy(currentTour.getPrivacy())
                    .locations(currentTour.getLocations().stream().map(locationMapper::toPublicLocationDTO).collect(Collectors.toList()))
                    .tags(currentTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(currentTour.getDepartLocation()))
                    .tourSchedules(tourScheduleBasicDTO)
                    .tourImages(currentTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                    .tourDays(currentTour.getTourDays().stream().map(tourDayMapper::toPublicTourDayDTO).collect(Collectors.toList()))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DETAIL_LOAD_SUCCESS, tourBasicDTO);
        } catch (Exception ex){
            throw BusinessException.of(TOUR_DETAIL_LOAD_FAIL, ex);
        }
    }

    private TourSimpleDTO convertToTourSimpleDTO(Tour tour) {
        return TourSimpleDTO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .highlights(tour.getHighlights())
                .numberDays(tour.getNumberDays())
                .numberNight(tour.getNumberNights())
                .note(tour.getNote())
                .deleted(tour.getDeleted())
                .tourType(tour.getTourType())
                .markUpPercent(tour.getMarkUpPercent())
                .privacy(tour.getPrivacy())
                .createdUserId(tour.getCreatedBy().getId())
                .createdUserName(tour.getCreatedBy().getFullName())
                .build();
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<Tour> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }

    private Specification<Tour> buildSearchSpecification(String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted tours
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("tourType"), "SIC"));
            predicates.add(cb.equal(root.get("tourStatus"), "OPENED"));

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

            LocalDate currentDate = LocalDate.now();

            // Filter by tour schedule date
            Join<Tour, TourSchedule> scheduleJoin = root.join("tourSchedules", JoinType.LEFT);
            predicates.add(cb.greaterThan(scheduleJoin.get("startDate"), currentDate.plusDays(1)));
            if (fromDate != null) {
                predicates.add(cb.greaterThan(scheduleJoin.get("startDate"), fromDate));
            }

            //Filter by price of tour
            Join<Tour, TourPax> paxJoin = root.join("tourPax", JoinType.LEFT);
            Predicate validToPredicate = cb.greaterThan(paxJoin.get("validTo"), currentDate);
            predicates.add(validToPredicate);

            if(budgetFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(paxJoin.get("sellingPrice"), budgetFrom));
            }

            if(budgetTo!= null) {
                predicates.add(cb.lessThanOrEqualTo(paxJoin.get("sellingPrice"), budgetTo));
            }

            if(departLocationId!= null) {
                predicates.add(cb.equal(root.get("depart_location").get("id"), departLocationId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    private Specification<Tour> buildSimpleSearchSpecification(String keyword, Boolean isDeleted, Boolean isOpened) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate namePredicate = cb.like(normalizedName, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                predicates.add(namePredicate);
            }

            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }
            if (isOpened != null) {
                predicates.add(cb.equal(root.get("opened"), isOpened));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private GeneralResponse<PagingDTO<List<TourSimpleDTO>>> buildSimplePagedResponse(Page<Tour> tourPage, List<TourSimpleDTO> tourDTOs) {
        PagingDTO<List<TourSimpleDTO>> pagingDTO = PagingDTO.<List<TourSimpleDTO>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tourDTOs)
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }

}
