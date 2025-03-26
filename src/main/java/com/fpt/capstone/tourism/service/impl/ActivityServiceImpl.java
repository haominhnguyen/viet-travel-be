package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.GeoPositionRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.ActivityCategoryMapper;
import com.fpt.capstone.tourism.mapper.ActivityMapper;
import com.fpt.capstone.tourism.mapper.GeoPositionMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.ActivityService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.service.impl.ServiceProviderServiceImpl.removeAccents;


@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final GeoPositionMapper geoPositionMapper;
    private final LocationMapper locationMapper;
    private final ActivityCategoryMapper activityCategoryMapper;
    private final TourRepository tourRepository;
    private final TourDayActivityRepository tourDayActivityRepository;
    private final ActivityCategoryRepository activityCategoryRepository;
    private final TourPaxRepository tourPaxRepository;

    @Override
    public List<ActivityDTO> findRecommendedActivities(int numberActivity) {
        List<Activity> randomActivities = activityRepository.findRandomActivities(numberActivity);
        return randomActivities.stream()
                .map(activityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GeneralResponse<ActivityDTO> saveActivity(ActivityDTO activityDTO) {
        try{
            //Validate input data
            Validator.validateActivity(activityDTO);

            //Check duplicate location
            if(activityRepository.findByTitle(activityDTO.getTitle()) != null){
                throw BusinessException.of(EXISTED_LOCATION);
            }

            //Save date to database
            Activity activity = activityMapper.toEntity(activityDTO);
            activity.setCreatedAt(LocalDateTime.now());
            activity.setDeleted(false);
            activityRepository.save(activity);

            ActivityDTO responseActivityDTO = activityMapper.toDTO(activity);
            responseActivityDTO.setId(activity.getId());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Create activity successfully", responseActivityDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of("Create activity fail", ex);
        }
    }

    @Override
    public GeneralResponse<ActivityDTO> getActivityById(Long id) {
        try{
            Activity activity = activityRepository.findById(id).orElseThrow();

            ActivityDTO activityDTO = activityMapper.toDTO(activity);
            return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, activityDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<ActivityDTO>>> getAllActivity(int page, int size, String keyword, Boolean isDeleted, String orderDate, Long categoryId) {
        try {
            // Determine sorting order dynamically
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Specification<Activity> spec = buildSearchSpecification(keyword, isDeleted, categoryId);

            Page<Activity> activityPage = activityRepository.findAll(spec, pageable);
            List<ActivityDTO> activityDTOS = activityPage.getContent().stream()
                    .map(activityMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(activityPage, activityDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("not ok", ex);
        }
    }

    @Override
    public GeneralResponse<ActivityDTO> updateActivity(Long id, ActivityDTO activityDTO) {
        try{
            //Find in database
            Activity activity = activityRepository.findById(id).orElseThrow();

            //Validate input data
            Validator.validateActivity(activityDTO);

            //Update activity information
            if(!activityDTO.getTitle().equals(activity.getTitle())){
                //Check duplicate activity
                if(activityRepository.findByTitle(activityDTO.getTitle()) != null){
                    throw BusinessException.of(ACTIVITY_EXISTED);
                }
                activity.setTitle(activityDTO.getTitle());
            }
            if(!activityDTO.getContent().equals(activity.getContent())){
                activity.setContent(activityDTO.getContent());
            }
            if(!activityDTO.getImageUrl().equals(activity.getImageUrl())){
                activity.setImageUrl(activityDTO.getImageUrl());
            }

            GeoPositionRequestDTO currentGeoPosition = GeoPositionRequestDTO.builder()
                    .latitude(activity.getGeoPosition().getLatitude())
                    .longitude(activity.getGeoPosition().getLongitude())
                    .build();
            if(!currentGeoPosition.equals(activityDTO.getGeoPosition())){
                activity.setGeoPosition(geoPositionMapper.toEntity(activityDTO.getGeoPosition()));
            }

            if(activityDTO.getPricePerPerson() != activity.getPricePerPerson()){
                activity.setPricePerPerson(activityDTO.getPricePerPerson());
            }

            if(activityDTO.getLocation().getId() != activity.getLocation().getId()){
                activity.setLocation(locationMapper.toEntity(activityDTO.getLocation()));
            }

            if(activityDTO.getActivityCategory().getId() != activity.getActivityCategory().getId()){
                activity.setActivityCategory(activityCategoryMapper.toEntity(activityDTO.getActivityCategory()));
            }

            activity.setUpdatedAt(LocalDateTime.now());

            activityRepository.save(activity);

            ActivityDTO reponseActivityDTO = activityMapper.toDTO(activity);
            reponseActivityDTO.setId(activity.getId());

            return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, reponseActivityDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<ActivityDTO> deleteActivity(Long id, boolean isDeleted) {
        try{
            Activity activity = activityRepository.findById(id).orElseThrow();

            activity.setDeleted(isDeleted);
            activity.setUpdatedAt(LocalDateTime.now());
            activityRepository.save(activity);

            ActivityDTO activityDTO = activityMapper.toDTO(activity);
            return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, activityDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public List<ActivityDTO> findRelatedActivities(Long activityId, int numberActivity) {
        try{
            Long locationId = activityRepository.findById(activityId).orElseThrow().getLocation().getId();
            return activityRepository.findRelatedActivities(locationId, numberActivity)
                    .stream().map(activityMapper::toDTO).collect(Collectors.toList());
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<List<ActivityListDTO>> getActivityList(Long tourId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Get all tour day activities for this tour
            List<TourDayActivity> tourDayActivities = tourDayActivityRepository.findByTourDayTourIdAndDeletedFalse(tourId);

            List<ActivityListDTO> activityList = new ArrayList<>();

            for (TourDayActivity tourDayActivity : tourDayActivities) {
                TourDay tourDay = tourDayActivity.getTourDay();
                Activity activity = tourDayActivity.getActivity();

                // Calculate pax prices for this activity
                List<TourPax> paxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);

                ActivityListDTO activityDTO = ActivityListDTO.builder()
                        .id(activity.getId())
                        .dayNumber(tourDay.getDayNumber())
                        .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                        .activityName(activity.getTitle())
                        .categoryName(activity.getActivityCategory() != null ? activity.getActivityCategory().getName() : null)
                        .pricePerPerson(activity.getPricePerPerson())
                        .build();

                activityList.add(activityDTO);
            }

            return new GeneralResponse<>(HttpStatus.OK.value(), ACTIVITY_LIST_LOAD_SUCCESS, activityList);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, ACTIVITY_LIST_LOAD_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<ActivityDetailDTO> getActivityDetail(Long tourId, Long activityId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Get activity
            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, ACTIVITY_NOT_FOUND + " with id: " + activityId));

            // 3. Find the TourDayActivity entry
            TourDayActivity tourDayActivity = tourDayActivityRepository.findByActivityIdAndTourDayTourId(activityId, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, ACTIVITY_NOT_ASSOCIATED));

            TourDay tourDay = tourDayActivity.getTourDay();
            // 4. Get pax options and calculate adjusted prices
            List<TourPax> paxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);
            // 5. Build response
            ActivityDetailDTO response = ActivityDetailDTO.builder()
                    .id(activity.getId())
                    .title(activity.getTitle())
                    .content(activity.getContent())
                    .imageUrl(activity.getImageUrl())
                    .dayNumber(tourDay.getDayNumber())
                    .locationId(tourDay.getLocation() != null ? tourDay.getLocation().getId() : null)
                    .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                    .categoryId(activity.getActivityCategory() != null ? activity.getActivityCategory().getId() : null)
                    .categoryName(activity.getActivityCategory() != null ? activity.getActivityCategory().getName() : null)
                    .pricePerPerson(activity.getPricePerPerson())
                    .numberTicket(tourDayActivity.getNumberTicket())
                    .latitude(activity.getGeoPosition() != null ? activity.getGeoPosition().getLatitude() : null)
                    .longitude(activity.getGeoPosition() != null ? activity.getGeoPosition().getLongitude() : null)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), ACTIVITY_DETAIL_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, ACTIVITY_DETAIL_LOAD_FAIL, ex);
        }
    }

    private Specification<Activity> buildSearchSpecification(String keyword, Boolean isDeleted, Long categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by isDeleted status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }

            // 🔹 Filter by categoryId (if provided)
            if (categoryId != null) {
                Join<Activity, ActivityCategory> categoryJoin = root.join("activityCategory", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }

            // Normalize text for search (Ignore case & accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Normalize keyword before passing into the query
                String normalizedKeyword = removeAccents(keyword.toLowerCase());

                // Normalize Activity Title
                Expression<String> normalizedActivityTitle = cb.function("unaccent", String.class, cb.lower(root.get("title")));

                // Normalize Location Name
                Join<Activity, Location> locationJoin = root.join("location", JoinType.LEFT);
                Expression<String> normalizedLocationName = cb.function("unaccent", String.class, cb.lower(locationJoin.get("name")));

                // Normalize Activity Category Name
                Join<Activity, ActivityCategory> categoryJoin = root.join("activityCategory", JoinType.LEFT);
                Expression<String> normalizedCategoryName = cb.function("unaccent", String.class, cb.lower(categoryJoin.get("name")));

                // Compare using LIKE
                Predicate activityTitlePredicate = cb.like(normalizedActivityTitle, "%" + normalizedKeyword + "%");
                Predicate locationNamePredicate = cb.like(normalizedLocationName, "%" + normalizedKeyword + "%");
                Predicate categoryNamePredicate = cb.like(normalizedCategoryName, "%" + normalizedKeyword + "%");

                // Match title OR location OR category
                predicates.add(cb.or(activityTitlePredicate, locationNamePredicate, categoryNamePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    private GeneralResponse<PagingDTO<List<ActivityDTO>>> buildPagedResponse(Page<Activity> activityPage, List<ActivityDTO> activityDTOS) {
        PagingDTO<List<ActivityDTO>> pagingDTO = PagingDTO.<List<ActivityDTO>>builder()
                .page(activityPage.getNumber())
                .size(activityPage.getSize())
                .total(activityPage.getTotalElements())
                .items(activityDTOS)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }
}
