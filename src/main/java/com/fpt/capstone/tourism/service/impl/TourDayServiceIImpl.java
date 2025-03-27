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
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Location not found"));

                // Only verify that categories are available in this location
                // but don't create any TourDayService entries yet
                verifyServiceCategoriesAvailableInLocation(request.getServiceCategories(), location.getId());
            }

            Integer maxDays = Math.max(tour.getNumberDays(), tour.getNumberNights());

            Integer dayNumber = tourDayRepository.findMaxDayNumberByTourId(tourId)
                    .map(maxDay -> maxDay + 1)
                    .orElse(1);

            if (dayNumber > maxDays) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST,
                        "Cannot create more days than the maximum defined in the tour (" + maxDays + " days/nights)");
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

            // Get service categories but DON'T create TourDayService entries yet
            List<ServiceCategory> serviceCategories = new ArrayList<>();

            for (String categoryName : request.getServiceCategories()) {
                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service category not found: " + categoryName));
                serviceCategories.add(category);

                // Store the association between tour day and service category
                // using the entity references
                saveTourDayServiceCategory(tourDay, category);
            }

            // Extract category names for response
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
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create tour day", ex);
        }
    }


    @Override
    @Transactional
    public GeneralResponse<TourDayFullDTO> updateTourDay(Long id, Long tourId, TourDayUpdateRequestDTO request) {
        try {
            // Validate service categories
            validateServiceCategories(request.getServiceCategories());

            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            TourDay tourDay = tourDayRepository.findByIdAndTourId(id, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));

            Location location = null;

            if (request.getLocationId() != null) {
                location = locationRepository.findById(request.getLocationId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Location not found"));

                // Only verify that categories are available in this location
                // but don't create any TourDayService entries yet
                verifyServiceCategoriesAvailableInLocation(request.getServiceCategories(), location.getId());
            }

            // Check if the day number already exists for another tour day in the same tour
            if (request.getDayNumber() != null && !request.getDayNumber().equals(tourDay.getDayNumber())) {
                // Check if the day number already exists for another tour day in the same tour
                boolean dayNumberExists = tourDayRepository.existsByTourIdAndDayNumberAndIdNot(
                        tourId, request.getDayNumber(), id);
                if (dayNumberExists) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            "Day number " + request.getDayNumber() + " already exists for this tour");
                }

                // Validate against max days/nights
                Integer maxDays = Math.max(tour.getNumberDays(), tour.getNumberNights());
                if (request.getDayNumber() > maxDays) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            "Day number cannot exceed the maximum defined in the tour (" + maxDays + " days/nights)");
                }
            }

            // Update tour day
            tourDay.setDayNumber(request.getDayNumber());
            tourDay.setTitle(request.getTitle());
            tourDay.setContent(request.getContent());
            tourDay.setMealPlan(request.getMealPlan());
            tourDay.setLocation(location);

            tourDay = tourDayRepository.save(tourDay);
            // Get requested service categories
            Set<String> requestedCategories = new HashSet<>(request.getServiceCategories());
            List<ServiceCategory> serviceCategories = new ArrayList<>();
            // Delete all existing tour day service category associations first
            deleteTourDayServiceCategories(tourDay);

            // Then add the new ones
            for (String categoryName : requestedCategories) {
                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service category not found: " + categoryName));
                serviceCategories.add(category);

                saveTourDayServiceCategory(tourDay, category);
            }

            // Extract category names for response
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
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update tour day", ex);
        }
    }
//    @Override
//    @Transactional
//    public GeneralResponse<TourDayFullDTO> createTourDay(Long tourId, TourDayCreateRequestDTO request) {
//        try {
//            // Validate service categories
//            validateServiceCategories(request.getServiceCategories());
//
//            Tour tour = tourRepository.findById(tourId)
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
//
//            Location location = null;
//            if (request.getLocationId() != null) {
//                location = locationRepository.findById(request.getLocationId())
//                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Location not found"));
//            }
//
//            Integer dayNumber = tourDayRepository.findMaxDayNumberByTourId(tourId)
//                    .map(maxDay -> maxDay + 1)
//                    .orElse(1);
//
//            TourDay tourDay = TourDay.builder()
//                    .dayNumber(dayNumber)
//                    .title(request.getTitle())
//                    .content(request.getContent())
//                    .mealPlan(request.getMealPlan())
//                    .deleted(false)
//                    .tour(tour)
//                    .location(location)
//                    .build();
//
//            tourDay = tourDayRepository.save(tourDay);
//
//            // Get service categories and create TourDayService entries
//            List<ServiceCategory> serviceCategories = new ArrayList<>();
//            List<com.fpt.capstone.tourism.model.TourDayService> createdTourDayServices = new ArrayList<>();
//
//            for (String categoryName : request.getServiceCategories()) {
//                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
//                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service category not found: " + categoryName));
//                serviceCategories.add(category);
//
//                // Find a default service for this category to create a TourDayService entry
//                List<Service> servicesForCategory = serviceRepository.findByServiceCategoryIdAndDeletedFalseOrderByIdDesc(category.getId());
//                if (!servicesForCategory.isEmpty()) {
//                    Service defaultService = servicesForCategory.get(0);
//                    // Create and save a TourDayService entry
//                    com.fpt.capstone.tourism.model.TourDayService tourDayService = new com.fpt.capstone.tourism.model.TourDayService();
//                    tourDayService.setTourDay(tourDay);
//                    tourDayService.setService(defaultService);
//                    tourDayService.setQuantity(1);
//                    tourDayService.setSellingPrice(defaultService.getSellingPrice());
//                    tourDayService = tourDayServiceRepository.save(tourDayService);
//                    createdTourDayServices.add(tourDayService);
//                }
//            }
//
//            // Get all tour day services for this tour day
//            List<com.fpt.capstone.tourism.model.TourDayService> tourDayServicesList = tourDayServiceRepository.findByTourDayId(tourDay.getId());
//
//            List<TourDayServiceFullDTO> tourDayServices = tourDayServicesList.stream()
//                    .map(tourDayService -> {
//                        Service service = tourDayService.getService();
//                        String categoryName = null;
//
//                        // Get the category name if service and category exist
//                        if (service != null && service.getServiceCategory() != null) {
//                            categoryName = service.getServiceCategory().getCategoryName();
//                        }
//
//                        return TourDayServiceFullDTO.builder()
//                                .id(tourDayService.getId())
//                                .serviceId(service != null ? service.getId() : null)
//                                .serviceName(service != null ? service.getName() : null)
//                                .serviceCategoryName(categoryName)
//                                .quantity(tourDayService.getQuantity())
//                                .sellingPrice(tourDayService.getSellingPrice())
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            List<String> categoryNames = serviceCategories.stream()
//                    .map(ServiceCategory::getCategoryName)
//                    .collect(Collectors.toList());
//
//            TourDayFullDTO tourDayDTO = TourDayFullDTO.builder()
//                    .id(tourDay.getId())
//                    .title(tourDay.getTitle())
//                    .dayNumber(tourDay.getDayNumber())
//                    .content(tourDay.getContent())
//                    .mealPlan(tourDay.getMealPlan())
//                    .tourId(tour.getId())
//                    .location(locationMapper.toDTO(location))
//                    .tourDayServices(tourDayServices)
//                    .serviceCategories(categoryNames)
//                    .deleted(false)
//                    .createdAt(tourDay.getCreatedAt())
//                    .updatedAt(tourDay.getUpdatedAt())
//                    .build();
//            return new GeneralResponse<>(HttpStatus.CREATED.value(), TOUR_DAY_CREATED_SUCCESS, tourDayDTO);
//        } catch (BusinessException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create tour day", ex);
//        }
//    }
//
//    @Override
//    @Transactional
//    public GeneralResponse<TourDayFullDTO> updateTourDay(Long id, Long tourId, TourDayUpdateRequestDTO request) {
//        try {
//            // Validate service categories
//            validateServiceCategories(request.getServiceCategories());
//
//            Location location = null;
//            if (request.getLocationId() != null) {
//                location = locationRepository.findById(request.getLocationId())
//                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Location not found"));
//            }
//
//            Tour tour = tourRepository.findById(tourId)
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
//
//            TourDay tourDay = tourDayRepository.findByIdAndTourId(id, tourId)
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));
//
//            // Check if the day number already exists for another tour day in the same tour
//            if (request.getDayNumber() != null && !request.getDayNumber().equals(tourDay.getDayNumber())) {
//                boolean dayNumberExists = tourDayRepository.existsByTourIdAndDayNumberAndIdNot(
//                        tourId, request.getDayNumber(), id);
//                if (dayNumberExists) {
//                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
//                            "Day number " + request.getDayNumber() + " already exists for this tour");
//                }
//            }
//
//            // Update tour day
//            tourDay.setDayNumber(request.getDayNumber());
//            tourDay.setTitle(request.getTitle());
//            tourDay.setContent(request.getContent());
//            tourDay.setMealPlan(request.getMealPlan());
//            tourDay.setLocation(location);
//
//            tourDay = tourDayRepository.save(tourDay);
//
//            // Get requested service categories
//            Set<String> requestedCategories = new HashSet<>(request.getServiceCategories());
//            List<ServiceCategory> serviceCategories = new ArrayList<>();
//
//            for (String categoryName : requestedCategories) {
//                ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
//                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service category not found: " + categoryName));
//                serviceCategories.add(category);
//            }
//
//            // Get existing tour day services
//            List<com.fpt.capstone.tourism.model.TourDayService> existingTourDayServices =
//                    tourDayServiceRepository.findByTourDayId(tourDay.getId());
//
//            // Track existing categories
//            Set<Long> existingCategoryIds = existingTourDayServices.stream()
//                    .map(tds -> tds.getService().getServiceCategory().getId())
//                    .collect(Collectors.toSet());
//
//            // Add new service categories that don't exist yet
//            for (ServiceCategory category : serviceCategories) {
//                if (!existingCategoryIds.contains(category.getId())) {
//                    // Find a default service for this category
//                    List<Service> servicesForCategory = serviceRepository.findByServiceCategoryIdAndDeletedFalseOrderByIdDesc(category.getId());
//                    if (!servicesForCategory.isEmpty()) {
//                        Service defaultService = servicesForCategory.get(0);
//
//                        // Create and save a TourDayService entry
//                        com.fpt.capstone.tourism.model.TourDayService tourDayService = new com.fpt.capstone.tourism.model.TourDayService();
//                        tourDayService.setTourDay(tourDay);
//                        tourDayService.setService(defaultService);
//                        tourDayService.setQuantity(1); // Default quantity
//                        tourDayService.setSellingPrice(defaultService.getSellingPrice());
//
//                        tourDayServiceRepository.save(tourDayService);
//                    }
//                }
//            }
//
//            // Remove tour day services for categories that are no longer in the request
//            Set<Long> requestedCategoryIds = serviceCategories.stream()
//                    .map(ServiceCategory::getId)
//                    .collect(Collectors.toSet());
//
//            for (com.fpt.capstone.tourism.model.TourDayService tds : existingTourDayServices) {
//                Long categoryId = tds.getService().getServiceCategory().getId();
//                if (!requestedCategoryIds.contains(categoryId)) {
//                    tourDayServiceRepository.delete(tds);
//                }
//            }
//
//            // Get updated tour day services after modifications
//            List<com.fpt.capstone.tourism.model.TourDayService> updatedTourDayServices =
//                    tourDayServiceRepository.findByTourDayId(tourDay.getId());
//
//            List<TourDayServiceFullDTO> tourDayServices = updatedTourDayServices.stream()
//                    .map(tourDayService -> {
//                        Service service = tourDayService.getService();
//                        String categoryName = null;
//
//                        // Get the category name if service and category exist
//                        if (service != null && service.getServiceCategory() != null) {
//                            categoryName = service.getServiceCategory().getCategoryName();
//                        }
//
//                        return TourDayServiceFullDTO.builder()
//                                .id(tourDayService.getId())
//                                .serviceId(service != null ? service.getId() : null)
//                                .serviceName(service != null ? service.getName() : null)
//                                .serviceCategoryName(categoryName)
//                                .quantity(tourDayService.getQuantity())
//                                .sellingPrice(tourDayService.getSellingPrice())
//                                .build();
//                    })
//                    .collect(Collectors.toList());
//
//            // Extract service category names for response
//            List<String> categoryNames = serviceCategories.stream()
//                    .map(ServiceCategory::getCategoryName)
//                    .collect(Collectors.toList());
//
//            TourDayFullDTO tourDayDTO = TourDayFullDTO.builder()
//                    .id(tourDay.getId())
//                    .title(tourDay.getTitle())
//                    .dayNumber(tourDay.getDayNumber())
//                    .content(tourDay.getContent())
//                    .mealPlan(tourDay.getMealPlan())
//                    .tourId(tour.getId())
//                    .tourDayServices(tourDayServices)
//                    .location(locationMapper.toDTO(location))
//                    .serviceCategories(categoryNames)
//                    .deleted(false)
//                    .createdAt(tourDay.getCreatedAt())
//                    .updatedAt(tourDay.getUpdatedAt())
//                    .build();
//            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DAY_UPDATED_SUCCESS, tourDayDTO);
//        } catch (BusinessException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update tour day", ex);
//        }
//    }

    @Override
    @Transactional
    public GeneralResponse<String> changeTourDayStatus(Long id, Long tourId, Boolean isDeleted) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            TourDay tourDay = tourDayRepository.findByIdAndTourId(id, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));

            // Set the deleted status based on the parameter
            tourDay.setDeleted(isDeleted);
            tourDayRepository.save(tourDay);

            String statusMessage = isDeleted ? "deleted" : "restored";
            String responseMessage = "Tour day with ID " + id + " has been " + statusMessage + " successfully";

            return new GeneralResponse<>(HttpStatus.OK.value(),
                    isDeleted ? TOUR_DAY_DELETED_SUCCESS : "Tour day restored successfully",
                    responseMessage);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            String action = isDeleted ? "delete" : "restore";
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to " + action + " tour day", ex);
        }
    }

    private void validateServiceCategories(List<String> serviceCategories) {
        List<String> validCategories = Arrays.asList("Hotel", "Restaurant", "Transport");

        if (serviceCategories == null || serviceCategories.isEmpty()) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, "At least one service category is required");
        }

        for (String category : serviceCategories) {
            if (!validCategories.contains(category)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_SERVICE_CATEGORY);
            }
        }
    }

    private void verifyServiceCategoriesAvailableInLocation(List<String> categories, Long locationId) {
        for (String category : categories) {
            boolean isAvailable = serviceProviderRepository.existsByLocationIdAndCategoryName(locationId, category);
            if (!isAvailable) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST,
                        String.format("No service provider available for category %s in the selected location", category));
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

