package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourDayFullMapper;
import com.fpt.capstone.tourism.mapper.TourDayServiceMapper;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourDayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TourDayServiceImpl implements TourDayService {
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
                        .map(tourDayService -> new TourDayServiceFullDTO(
                                tourDayService.getId(),
                                tourDayService.getService().getId(),
                                tourDayService.getService().getName(),
                                tourDayService.getQuantity(),
                                tourDayService.getSellingPrice()
                        ))
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
    public GeneralResponse<TourDayFullDTO> updateTourDayDetail(Long tourDayId, TourDayUpdateDTO tourDayUpdateDTO) {
        try {
            // Find the tour day to update
            TourDay tourDay = tourDayRepository.findById(tourDayId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND));

            // Update the tour day fields
            if (tourDayUpdateDTO.getTitle() != null) {
                tourDay.setTitle(tourDayUpdateDTO.getTitle());
            }
            if (tourDayUpdateDTO.getContent() != null) {
                tourDay.setContent(tourDayUpdateDTO.getContent());
            }
            if (tourDayUpdateDTO.getMealPlan() != null) {
                tourDay.setMealPlan(tourDayUpdateDTO.getMealPlan());
            }

            // Update location if provided
            if (tourDayUpdateDTO.getLocationId() != null) {
                Location location = locationRepository.findById(tourDayUpdateDTO.getLocationId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND));
                tourDay.setLocation(location);
            }

            // Save the updated tour day
            tourDay = tourDayRepository.save(tourDay);

            // Handle tour day services updates
            if (tourDayUpdateDTO.getTourDayServices() != null && !tourDayUpdateDTO.getTourDayServices().isEmpty()) {
                // Get current tour day services
                List<com.fpt.capstone.tourism.model.TourDayService> existingServices = tourDayServiceRepository.findByTourDayId(tourDay.getId());
                Set<Long> updatedServiceIds = new HashSet<>();

                // Process each service in the update request
                for (TourDayServiceRequestDTO serviceDTO : tourDayUpdateDTO.getTourDayServices()) {
                    if (serviceDTO.getId() != null) {
                        // Update existing service
                        com.fpt.capstone.tourism.model.TourDayService existingService = existingServices.stream()
                                .filter(s -> s.getId().equals(serviceDTO.getId()))
                                .findFirst()
                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_SERVICE_NOT_FOUND));

                        // Update service if serviceId changed
                        if (serviceDTO.getServiceId() != null && !existingService.getService().getId().equals(serviceDTO.getServiceId())) {
                            Service service = serviceRepository.findById(serviceDTO.getServiceId())
                                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));
                            existingService.setService(service);
                        }

                        // Update quantity if provided
                        if (serviceDTO.getQuantity() != null) {
                            existingService.setQuantity(serviceDTO.getQuantity());
                        }

                        // Update selling price if provided
                        if (serviceDTO.getSellingPrice() != null) {
                            existingService.setSellingPrice(serviceDTO.getSellingPrice());
                        }

                        tourDayServiceRepository.save(existingService);
                        updatedServiceIds.add(existingService.getId());
                    } else {
                        // Add new service
                        if (serviceDTO.getServiceId() == null) {
                            throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_ID_REQUIRED);
                        }

                        Service service = serviceRepository.findById(serviceDTO.getServiceId())
                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

                        com.fpt.capstone.tourism.model.TourDayService newService = new com.fpt.capstone.tourism.model.TourDayService();
                        newService.setTourDay(tourDay);
                        newService.setService(service);
                        newService.setQuantity(serviceDTO.getQuantity() != null ? serviceDTO.getQuantity() : 1);
                        newService.setSellingPrice(serviceDTO.getSellingPrice());

                        com.fpt.capstone.tourism.model.TourDayService saved = tourDayServiceRepository.save(newService);
                        updatedServiceIds.add(saved.getId());
                    }
                }

                // Delete services that weren't included in the update request
                existingServices.stream()
                        .filter(service -> !updatedServiceIds.contains(service.getId()))
                        .forEach(tourDayServiceRepository::delete);
            }

            // Return updated tour day details
            List<TourDay> singleTourDay = new ArrayList<>();
            singleTourDay.add(tourDay);

            List<TourDayFullDTO> tourDayDTOs = singleTourDay.stream().map(td -> {
                List<com.fpt.capstone.tourism.model.TourDayService> tourDayServicesList =
                        tourDayServiceRepository.findByTourDayId(td.getId());

                List<TourDayServiceFullDTO> tourDayServices = tourDayServicesList.stream()
                        .map(tourDayService -> new TourDayServiceFullDTO(
                                tourDayService.getId(),
                                tourDayService.getService().getId(),
                                tourDayService.getService().getName(),
                                tourDayService.getQuantity(),
                                tourDayService.getSellingPrice()
                        ))
                        .collect(Collectors.toList());

                return TourDayFullDTO.builder()
                        .id(td.getId())
                        .title(td.getTitle())
                        .content(td.getContent())
                        .mealPlan(td.getMealPlan())
                        .tourId(td.getTour().getId())
                        .location(locationMapper.toDTO(td.getLocation()))
                        .tourDayServices(tourDayServices)
                        .createdAt(td.getCreatedAt())
                        .updatedAt(td.getUpdatedAt())
                        .build();
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DAY_UPDATE_SUCCESS, tourDayDTOs.get(0));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_DAY_UPDATE_FAIL, ex);
        }
    }
}
