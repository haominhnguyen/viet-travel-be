package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourDayFullMapper;
import com.fpt.capstone.tourism.mapper.TourDayServiceMapper;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.repository.TourDayRepository;
import com.fpt.capstone.tourism.repository.TourDayServiceRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.service.TourDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
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

}
