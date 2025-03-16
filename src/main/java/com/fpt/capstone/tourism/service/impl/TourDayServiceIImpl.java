package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourDayFullMapper;
import com.fpt.capstone.tourism.mapper.TourDayServiceMapper;
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
    private final TourDayFullMapper tourDayFullMapper;
    private final TourDayServiceMapper tourDayServiceMapper;
    private final LocationMapper locationMapper;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final ServiceRepository serviceRepository;
    private final LocationRepository locationRepository;

    @Override
    public GeneralResponse<List<TourDayFullDTO>> getTourDayDetail(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
            List<TourDay> tourDays = tourDayRepository.findByTourIdOrderById(tourId);
            if (tourDays.isEmpty()) {
                return new GeneralResponse<>(HttpStatus.OK.value(), NO_TOUR_DAY_FOUND, Collections.emptyList());
            }

            // Map list of TourDay to list of TourDayFullDTO
            List<TourDayFullDTO> tourDayDTOs = tourDays.stream().map(tourDay -> {
                // Get list of service IDs from TourDayService table
                List<Long> serviceIds = tourDayServiceRepository.findServiceIdsByTourDayId(tourDay.getId());

                // Get services by IDs
                List<Service> services = serviceRepository.findByIdIn(serviceIds);
                List<com.fpt.capstone.tourism.model.TourDayService> tourDayServicesList = tourDayServiceRepository.findByTourDayId(tourDay.getId());

                List<TourDayServiceFullDTO> tourDayServices = tourDayServicesList.stream()
                        .map(tourDayService -> {
                            Service service = tourDayService.getService();
                            String categoryName = null;

                            // Get the category name if service and category exist
                            if (service != null && service.getServiceCategory() != null) {
                                categoryName = service.getServiceCategory().getCategoryName();
                            }

                            return TourDayServiceFullDTO.builder()
                                    .id(tourDayService.getId())
                                    .serviceId(service != null ? service.getId() : null)
                                    .serviceName(service != null ? service.getName() : null)
                                    .serviceCategoryName(categoryName)
                                    .quantity(tourDayService.getQuantity())
                                    .sellingPrice(tourDayService.getSellingPrice())
                                    .build();
                        })
                        .collect(Collectors.toList());

                return TourDayFullDTO.builder()
                        .id(tourDay.getId())
                        .title(tourDay.getTitle())
                        .content(tourDay.getContent())
                        .mealPlan(tourDay.getMealPlan())
                        .tourId(tour.getId())
                        .location(locationMapper.toDTO(tourDay.getLocation()))
                        .tourDayServices(tourDayServices)
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
    public GeneralResponse<TourDayFullDTO> createTourDay(TourDayCreateRequestDTO createRequestDTO) {
        try {
            // Validate input
            if (createRequestDTO == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Tour day request cannot be null");
            }

            // Check if tour exists
            Tour tour = tourRepository.findById(createRequestDTO.getTourId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Check if location exists
            Location location = locationRepository.findById(createRequestDTO.getLocationId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));

            // Create tour day entity
            TourDay tourDay = new TourDay();
            tourDay.setTitle(createRequestDTO.getTitle());
            tourDay.setContent(createRequestDTO.getContent());
            tourDay.setMealPlan(createRequestDTO.getMealPlan());
            tourDay.setTour(tour);
            tourDay.setLocation(location);
            tourDay.setDeleted(false);

            // Save tour day to get ID
            TourDay savedTourDay = tourDayRepository.save(tourDay);

            List<TourDayServiceFullDTO> tourDayServiceDTOs = new ArrayList<>();

            // Process tour day services if provided
            if (createRequestDTO.getTourDayServices() != null && !createRequestDTO.getTourDayServices().isEmpty()) {
                List<TourDayService> tourDayServiceIS = new ArrayList<>();

                for (TourDayServiceManageRequestDTO serviceDTO : createRequestDTO.getTourDayServices()) {
                    // Check if service exists
                    Service service = serviceRepository.findById(serviceDTO.getServiceId())
                            .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service not found"));

                    // Create tour day service
                    TourDayService tourDayService = new TourDayService();
                    tourDayService.setTourDay(savedTourDay);
                    tourDayService.setService(service);
                    tourDayService.setQuantity(serviceDTO.getQuantity());
                    tourDayService.setSellingPrice(serviceDTO.getSellingPrice());
                    tourDayServiceIS.add(tourDayService);
                }

                // Save all tour day services
                List<TourDayService> savedServices = tourDayServiceRepository.saveAll(tourDayServiceIS);

                // Map saved services to DTOs
                tourDayServiceDTOs = savedServices.stream()
                        .map(service -> {
                            String categoryName = null;
                            if (service.getService() != null && service.getService().getServiceCategory() != null) {
                                categoryName = service.getService().getServiceCategory().getCategoryName();
                            }

                            return TourDayServiceFullDTO.builder()
                                    .id(service.getId())
                                    .serviceId(service.getService().getId())
                                    .serviceName(service.getService().getName())
                                    .serviceCategoryName(categoryName)
                                    .quantity(service.getQuantity())
                                    .sellingPrice(service.getSellingPrice())
                                    .build();
                        })
                        .collect(Collectors.toList());
            }

            // Build response DTO
            TourDayFullDTO responseDTO = TourDayFullDTO.builder()
                    .id(savedTourDay.getId())
                    .title(savedTourDay.getTitle())
                    .content(savedTourDay.getContent())
                    .mealPlan(savedTourDay.getMealPlan())
                    .tourId(tour.getId())
                    .location(locationMapper.toDTO(location))
                    .tourDayServices(tourDayServiceDTOs)
                    .createdAt(savedTourDay.getCreatedAt())
                    .updatedAt(savedTourDay.getUpdatedAt())
                    .build();
            return new GeneralResponse<>(HttpStatus.CREATED.value(), "Tour day created successfully", responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of("Failed to create tour day", ex);
        }
    }

    @Override
    public GeneralResponse<TourDayFullDTO> updateTourDay(Long tourDayId, TourDayUpdateRequestDTO updateRequestDTO) {
        try {
            // Validate input
            if (updateRequestDTO == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Tour day update request cannot be null");
            }

            // Find the tour day to update
            TourDay tourDay = tourDayRepository.findById(tourDayId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Tour day not found"));

            // Check if tour exists if tourId is provided
            if (updateRequestDTO.getTourId() != null) {
                Tour tour = tourRepository.findById(updateRequestDTO.getTourId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
                tourDay.setTour(tour);
            }

            // Check if location exists if locationId is provided
            if (updateRequestDTO.getLocationId() != null) {
                Location location = locationRepository.findById(updateRequestDTO.getLocationId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));
                tourDay.setLocation(location);
            }

            // Update basic tour day information
            if (updateRequestDTO.getTitle() != null) {
                tourDay.setTitle(updateRequestDTO.getTitle());
            }

            if (updateRequestDTO.getContent() != null) {
                tourDay.setContent(updateRequestDTO.getContent());
            }

            if (updateRequestDTO.getMealPlan() != null) {
                tourDay.setMealPlan(updateRequestDTO.getMealPlan());
            }

            // Save updated tour day
            TourDay savedTourDay = tourDayRepository.save(tourDay);

            // Handle tour day services if provided
            if (updateRequestDTO.getTourDayServices() != null) {
                // Get existing tour day services
                List<TourDayService> existingServices = tourDayServiceRepository.findByTourDayId(tourDayId);

                // Create a map of existing services by ID for easy access
                Map<Long, TourDayService> existingServiceMap = existingServices.stream()
                        .collect(Collectors.toMap(
                                TourDayService::getId,
                                service -> service,
                                (s1, s2) -> s1
                        ));

                // Keep track of processed service IDs to identify which ones to delete
                Set<Long> processedServiceIds = new HashSet<>();

                // List to hold services to save (new or updated)
                List<TourDayService> servicesToSave = new ArrayList<>();

                // Process each service in the request
                for (TourDayServiceUpdateRequestDTO serviceDTO : updateRequestDTO.getTourDayServices()) {
                    TourDayService tourDayService;

                    if (serviceDTO.getId() != null && existingServiceMap.containsKey(serviceDTO.getId())) {
                        // Update existing service
                        tourDayService = existingServiceMap.get(serviceDTO.getId());
                        processedServiceIds.add(serviceDTO.getId());
                    } else {
                        // Create new service
                        tourDayService = new TourDayService();
                        tourDayService.setTourDay(savedTourDay);
                    }

                    // Update service if serviceId is provided
                    if (serviceDTO.getServiceId() != null) {
                        Service service = serviceRepository.findById(serviceDTO.getServiceId())
                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service not found"));
                        tourDayService.setService(service);
                    }

                    // Update quantity and selling price if provided
                    if (serviceDTO.getQuantity() != null) {
                        tourDayService.setQuantity(serviceDTO.getQuantity());
                    }

                    if (serviceDTO.getSellingPrice() != null) {
                        tourDayService.setSellingPrice(serviceDTO.getSellingPrice());
                    }

                    servicesToSave.add(tourDayService);
                }

                // Save new and updated services
                List<TourDayService> savedServices = tourDayServiceRepository.saveAll(servicesToSave);

                // Mark services for deletion that weren't in the update request
                existingServices.stream()
                        .filter(service -> !processedServiceIds.contains(service.getId()))
                        .forEach(service -> {
                            servicesToSave.add(service);
                        });

                // Save services marked for deletion
                if (!servicesToSave.isEmpty()) {
                    tourDayServiceRepository.saveAll(servicesToSave);
                }
            }

            // Fetch the updated tour day with its services for the response
            TourDay updatedTourDay = tourDayRepository.findById(tourDayId).orElse(savedTourDay);
            List<TourDayService> updatedServices = tourDayServiceRepository.findByTourDayId(tourDayId);
            // Map services to DTOs
            List<TourDayServiceFullDTO> tourDayServiceDTOs = updatedServices.stream()
                    .map(service -> {
                        String categoryName = null;
                        if (service.getService() != null && service.getService().getServiceCategory() != null) {
                            categoryName = service.getService().getServiceCategory().getCategoryName();
                        }

                        return TourDayServiceFullDTO.builder()
                                .id(service.getId())
                                .serviceId(service.getService().getId())
                                .serviceName(service.getService().getName())
                                .serviceCategoryName(categoryName)
                                .quantity(service.getQuantity())
                                .sellingPrice(service.getSellingPrice())
                                .build();
                    })
                    .collect(Collectors.toList());

            // Build response DTO
            TourDayFullDTO responseDTO = TourDayFullDTO.builder()
                    .id(updatedTourDay.getId())
                    .title(updatedTourDay.getTitle())
                    .content(updatedTourDay.getContent())
                    .mealPlan(updatedTourDay.getMealPlan())
                    .tourId(updatedTourDay.getTour().getId())
                    .location(locationMapper.toDTO(updatedTourDay.getLocation()))
                    .tourDayServices(tourDayServiceDTOs)
                    .createdAt(updatedTourDay.getCreatedAt())
                    .updatedAt(updatedTourDay.getUpdatedAt())
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Tour day updated successfully", responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of("Failed to update tour day", ex);
        }
    }

//    @Override
//    @Transactional
//    public GeneralResponse<TourDayFullDTO> updateTourDayDetail(Long tourDayId, TourDayUpdateDTO tourDayUpdateDTO) {
//        try {
//            // Find the tour day to update
//            TourDay tourDay = tourDayRepository.findById(tourDayId)
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));
//
//            // Update the tour day fields
//            if (tourDayUpdateDTO.getTitle() != null) {
//                tourDay.setTitle(tourDayUpdateDTO.getTitle());
//            }
//            if (tourDayUpdateDTO.getContent() != null) {
//                tourDay.setContent(tourDayUpdateDTO.getContent());
//            }
//            if (tourDayUpdateDTO.getMealPlan() != null) {
//                tourDay.setMealPlan(tourDayUpdateDTO.getMealPlan());
//            }
//
//            // Update location if provided
//            if (tourDayUpdateDTO.getLocationId() != null) {
//                Location location = locationRepository.findById(tourDayUpdateDTO.getLocationId())
//                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));
//                tourDay.setLocation(location);
//            }
//
//            // Save the updated tour day
//            tourDay = tourDayRepository.save(tourDay);
//
//            // Handle tour day services updates
//            if (tourDayUpdateDTO.getTourDayServices() != null && !tourDayUpdateDTO.getTourDayServices().isEmpty()) {
//                // Get current tour day services
//                List<com.fpt.capstone.tourism.model.TourDayService> existingServices = tourDayServiceRepository.findByTourDayId(tourDay.getId());
//                Set<Long> updatedServiceIds = new HashSet<>();
//
//                // Process each service in the update request
//                for (TourDayServiceRequestDTO serviceDTO : tourDayUpdateDTO.getTourDayServices()) {
//                    if (serviceDTO.getId() != null) {
//                        // Update existing service
//                        com.fpt.capstone.tourism.model.TourDayService existingService = existingServices.stream()
//                                .filter(s -> s.getId().equals(serviceDTO.getId()))
//                                .findFirst()
//                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_SERVICE_NOT_FOUND));
//
//                        // Update service if serviceId changed
//                        if (serviceDTO.getServiceId() != null && !existingService.getService().getId().equals(serviceDTO.getServiceId())) {
//                            Service service = serviceRepository.findById(serviceDTO.getServiceId())
//                                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));
//                            existingService.setService(service);
//                        }
//
//                        // Update quantity if provided
//                        if (serviceDTO.getQuantity() != null) {
//                            existingService.setQuantity(serviceDTO.getQuantity());
//                        }
//
//                        // Update selling price if provided
//                        if (serviceDTO.getSellingPrice() != null) {
//                            existingService.setSellingPrice(serviceDTO.getSellingPrice());
//                        }
//
//                        tourDayServiceRepository.save(existingService);
//                        updatedServiceIds.add(existingService.getId());
//                    } else {
//                        // Add new service
//                        if (serviceDTO.getServiceId() == null) {
//                            throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_ID_REQUIRED);
//                        }
//
//                        Service service = serviceRepository.findById(serviceDTO.getServiceId())
//                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));
//
//                        com.fpt.capstone.tourism.model.TourDayService newService = new com.fpt.capstone.tourism.model.TourDayService();
//                        newService.setTourDay(tourDay);
//                        newService.setService(service);
//                        newService.setQuantity(serviceDTO.getQuantity() != null ? serviceDTO.getQuantity() : 1);
//                        newService.setSellingPrice(serviceDTO.getSellingPrice());
//
//                        com.fpt.capstone.tourism.model.TourDayService saved = tourDayServiceRepository.save(newService);
//                        updatedServiceIds.add(saved.getId());
//                    }
//                }
//
//                // Delete services that weren't included in the update request
//                existingServices.stream()
//                        .filter(service -> !updatedServiceIds.contains(service.getId()))
//                        .forEach(tourDayServiceRepository::delete);
//            }
//
//            // Return updated tour day details
//            List<TourDay> singleTourDay = new ArrayList<>();
//            singleTourDay.add(tourDay);
//
//            List<TourDayFullDTO> tourDayDTOs = singleTourDay.stream().map(td -> {
//                List<com.fpt.capstone.tourism.model.TourDayService> tourDayServicesList =
//                        tourDayServiceRepository.findByTourDayId(td.getId());
//
//                List<TourDayServiceFullDTO> tourDayServices = tourDayServicesList.stream()
//                        .map(tourDayService -> new TourDayServiceFullDTO(
//                                tourDayService.getId(),
//                                tourDayService.getService().getId(),
//                                tourDayService.getService().getName(),
//                                tourDayService.getService().getServiceCategory().getCategoryName(),
//                                tourDayService.getQuantity(),
//                                tourDayService.getSellingPrice()
//                        ))
//                        .collect(Collectors.toList());
//
//                return TourDayFullDTO.builder()
//                        .id(td.getId())
//                        .title(td.getTitle())
//                        .content(td.getContent())
//                        .mealPlan(td.getMealPlan())
//                        .tourId(td.getTour().getId())
//                        .location(locationMapper.toDTO(td.getLocation()))
//                        .tourDayServices(tourDayServices)
//                        .createdAt(td.getCreatedAt())
//                        .updatedAt(td.getUpdatedAt())
//                        .build();
//            }).collect(Collectors.toList());
//
//            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DAY_UPDATE_SUCCESS, tourDayDTOs.get(0));
//        } catch (BusinessException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw BusinessException.of(TOUR_DAY_UPDATE_FAIL, ex);
//        }
//    }
}
