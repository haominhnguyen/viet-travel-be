package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourDayServiceMapper;
import com.fpt.capstone.tourism.mapper.TourDayServiceResponseMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourDayServiceI;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TourDayServiceIImpl implements TourDayServiceI {

    private final TourDayRepository tourDayRepository;
    private final TourRepository tourRepository;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final TourDayServiceCategoryRepository tourDayServiceCategoryRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @Override
    public GeneralResponse<List<TourDayFullDTO>> getTourDayDetail(Long tourId, Boolean isDeleted) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
            // Get tour days with optional filter by deleted status
            List<TourDay> tourDays;
            if (isDeleted != null) {
                tourDays = tourDayRepository.findByTourIdAndDeletedOrderByDayNumber(tourId, isDeleted);
            } else {
                tourDays = tourDayRepository.findByTourIdOrderByDayNumber(tourId);
            }
            if (tourDays.isEmpty()) {
                return new GeneralResponse<>(HttpStatus.OK.value(), NO_TOUR_DAY_FOUND, Collections.emptyList());
            }
            // Map list of TourDay to list of TourDayFullDTO
            List<TourDayFullDTO> tourDayDTOs = tourDays.stream().map(tourDay -> {
                // Get service categories for this tour day using the entity reference approach
                List<String> serviceCategories = getServiceCategoriesForTourDay(tourDay);
                return TourDayFullDTO.builder()
                        .id(tourDay.getId())
                        .title(tourDay.getTitle())
                        .dayNumber(tourDay.getDayNumber())
                        .content(tourDay.getContent())
                        .mealPlan(tourDay.getMealPlan())
                        .tourId(tour.getId())
                        .location(locationMapper.toDTO(tourDay.getLocation()))
                        .serviceCategories(serviceCategories)
                        .deleted(tourDay.getDeleted())
                        .createdAt(tourDay.getCreatedAt())
                        .updatedAt(tourDay.getUpdatedAt())
                        .build();
            }).collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DAY_DETAIL_LOAD_SUCCESS, tourDayDTOs);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_DAY_DETAIL_LOAD_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourDayFullDTO> createTourDay(Long tourId, TourDayCreateRequestDTO request) {
        try {
            // Validate service categories
            validateServiceCategories(request.getServiceCategories());

            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            Location location = null;
            if (request.getLocationId() != null) {
                location = locationRepository.findById(request.getLocationId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));

                verifyServiceCategoriesAvailableInLocation(request.getServiceCategories(), location.getId());
            }

            Integer maxDays = Math.max(tour.getNumberDays(), tour.getNumberNights());

            Integer dayNumber = tourDayRepository.findMaxDayNumberByTourId(tourId)
                    .map(maxDay -> maxDay + 1)
                    .orElse(1);

            if (dayNumber > maxDays) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST,
                        TOUR_DAY_EXCEEDS_MAX_LIMIT + maxDays + " ngày/đêm");
            }

            TourDay tourDay = TourDay.builder()
                    .dayNumber(dayNumber)
                    .title(request.getTitle())
                    .content(request.getContent())
                    .mealPlan(request.getMealPlan())
                    .deleted(false)
                    .tour(tour)
                    .location(location)
                    .build();

            tourDay = tourDayRepository.save(tourDay);

            List<ServiceCategory> serviceCategories = new ArrayList<>();

            for (String categoryName : request.getServiceCategories()) {
                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND,
                                SERVICE_CATEGORY_NOT_FOUND + categoryName));
                serviceCategories.add(category);

                saveTourDayServiceCategory(tourDay, category);
            }

            List<String> categoryNames = serviceCategories.stream()
                    .map(ServiceCategory::getCategoryName)
                    .collect(Collectors.toList());

            TourDayFullDTO tourDayDTO = TourDayFullDTO.builder()
                    .id(tourDay.getId())
                    .title(tourDay.getTitle())
                    .dayNumber(tourDay.getDayNumber())
                    .content(tourDay.getContent())
                    .mealPlan(tourDay.getMealPlan())
                    .tourId(tour.getId())
                    .location(locationMapper.toDTO(location))
                    .serviceCategories(categoryNames)
                    .deleted(false)
                    .createdAt(tourDay.getCreatedAt())
                    .updatedAt(tourDay.getUpdatedAt())
                    .build();
            return new GeneralResponse<>(HttpStatus.CREATED.value(), TOUR_DAY_CREATED_SUCCESS, tourDayDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, TOUR_DAY_CREATE_FAILED, ex);
        }
    }


    @Override
    @Transactional
    public GeneralResponse<TourDayFullDTO> updateTourDay(Long id, Long tourId, TourDayUpdateRequestDTO request) {
        try {
            validateServiceCategories(request.getServiceCategories());
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            TourDay tourDay = tourDayRepository.findByIdAndTourId(id, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));

            Location location = null;
            if (request.getLocationId() != null) {
                location = locationRepository.findById(request.getLocationId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));
                verifyServiceCategoriesAvailableInLocation(request.getServiceCategories(), location.getId());
            }
            if (request.getDayNumber() != null && !request.getDayNumber().equals(tourDay.getDayNumber())) {
                boolean dayNumberExists = tourDayRepository.existsByTourIdAndDayNumberAndIdNot(
                        tourId, request.getDayNumber(), id);
                if (dayNumberExists) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            TOUR_DAY_NUMBER_ALREADY_EXISTS + request.getDayNumber());
                }
                Integer maxDays = Math.max(tour.getNumberDays(), tour.getNumberNights());
                if (request.getDayNumber() > maxDays) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            TOUR_DAY_EXCEEDS_MAX_LIMIT + maxDays + " ngày/đêm");
                }
            }
            tourDay.setDayNumber(request.getDayNumber());
            tourDay.setTitle(request.getTitle());
            tourDay.setContent(request.getContent());
            tourDay.setMealPlan(request.getMealPlan());
            tourDay.setLocation(location);
            tourDay = tourDayRepository.save(tourDay);

            Set<String> requestedCategories = new HashSet<>(request.getServiceCategories());
            List<ServiceCategory> serviceCategories = new ArrayList<>();

            deleteTourDayServiceCategories(tourDay);

            for (String categoryName : requestedCategories) {
                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND,
                                SERVICE_CATEGORY_NOT_FOUND + categoryName));
                serviceCategories.add(category);
                saveTourDayServiceCategory(tourDay, category);
            }

            List<String> categoryNames = serviceCategories.stream()
                    .map(ServiceCategory::getCategoryName)
                    .collect(Collectors.toList());

            TourDayFullDTO tourDayDTO = TourDayFullDTO.builder()
                    .id(tourDay.getId())
                    .title(tourDay.getTitle())
                    .dayNumber(tourDay.getDayNumber())
                    .content(tourDay.getContent())
                    .mealPlan(tourDay.getMealPlan())
                    .tourId(tour.getId())
                    .location(locationMapper.toDTO(location))
                    .serviceCategories(categoryNames)
                    .deleted(tourDay.getDeleted())
                    .createdAt(tourDay.getCreatedAt())
                    .updatedAt(tourDay.getUpdatedAt())
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DAY_UPDATED_SUCCESS, tourDayDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, TOUR_DAY_UPDATE_FAILED, ex);
        }
    }


    @Override
    @Transactional
    public GeneralResponse<String> changeTourDayStatus(Long id, Long tourId, Boolean isDeleted) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            TourDay tourDay = tourDayRepository.findByIdAndTourId(id, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));

            tourDay.setDeleted(isDeleted);
            tourDayRepository.save(tourDay);

            String responseMessage = isDeleted
                    ? "Ngày tour có ID " + id + " đã được xoá thành công."
                    : "Ngày tour có ID " + id + " đã được khôi phục thành công.";

            return new GeneralResponse<>(HttpStatus.OK.value(),
                    isDeleted ? TOUR_DAY_DELETED_SUCCESS : TOUR_DAY_RESTORED_SUCCESS,
                    responseMessage);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            String action = isDeleted ? "xoá" : "khôi phục";
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể " + action + " ngày tour", ex);
        }
    }


    private void validateServiceCategories(List<String> serviceCategories) {
        List<String> validCategories = Arrays.asList("Hotel", "Restaurant", "Transport", "Activity", "Flight Ticket");

        if (serviceCategories == null || serviceCategories.isEmpty()) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_CATEGORY_REQUIRED);
        }

        for (String category : serviceCategories) {
            if (!validCategories.contains(category)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_SERVICE_CATEGORY);
            }
        }
    }

    private void verifyServiceCategoriesAvailableInLocation(List<String> categories, Long locationId) {
        for (String category : categories) {
            if (TICKET.equals(category)) {
                continue;
            }
            boolean isAvailable = serviceProviderRepository.existsByLocationIdAndCategoryName(locationId, category);
            if (!isAvailable) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST,
                        String.format(NO_PROVIDER_FOR_CATEGORY_IN_LOCATION, category));
            }
        }
    }


    private void saveTourDayServiceCategory(TourDay tourDay, ServiceCategory serviceCategory) {
        TourDayServiceCategory association = new TourDayServiceCategory();
        association.setTourDay(tourDay);
        association.setServiceCategory(serviceCategory);
        tourDayServiceCategoryRepository.save(association);
    }

    private void deleteTourDayServiceCategories(TourDay tourDay) {
        tourDayServiceCategoryRepository.deleteByTourDay(tourDay);
    }

    private List<String> getServiceCategoriesForTourDay(TourDay tourDay) {
        List<TourDayServiceCategory> associations = tourDayServiceCategoryRepository.findByTourDay(tourDay);

        if (associations.isEmpty()) {
            return new ArrayList<>();
        }

        return associations.stream()
                .map(assoc -> assoc.getServiceCategory().getCategoryName())
                .collect(Collectors.toList());
    }
}

