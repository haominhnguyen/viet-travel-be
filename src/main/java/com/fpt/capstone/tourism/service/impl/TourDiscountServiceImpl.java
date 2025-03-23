package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TourDayRepository;
import com.fpt.capstone.tourism.repository.TourDayServiceRepository;
import com.fpt.capstone.tourism.repository.TourPaxRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.service.TourDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class TourDiscountServiceImpl implements TourDiscountService {
    private final TourRepository tourRepository;
    private final TourDayRepository tourDayRepository;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final TourPaxRepository tourPaxRepository;

    @Override
    public GeneralResponse<ServiceDetailDTO> getServiceDetail(Long tourId, Long serviceId) {
        return null;
    }

    @Override
    public GeneralResponse<ServiceProviderServicesDTO> getServiceProviderServices(Long providerId, Long locationId) {
        return null;
    }

    @Override
    public GeneralResponse<ServiceDetailDTO> updateServiceDetail(Long tourId, Long serviceId, ServiceUpdateRequestDTO request) {
        return null;
    }

    @Override
    public GeneralResponse<TourServiceListDTO> getTourServicesList(Long tourId, Integer paxCount) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get all tour days for this tour
            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);
            if (tourDays.isEmpty()) {
                throw BusinessException.of(HttpStatus.NOT_FOUND, NO_TOUR_DAYS_FOUND + " for tour with id: " + tourId);
            }

            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            // Get all tour day services
            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            // Get pax options
            List<TourPax> paxOptions = paxCount != null
                    ? tourPaxRepository.findByTourIdAndPaxRange(tourId, paxCount)
                    : tourPaxRepository.findByTourIdOrderByMinPax(tourId);

            List<TourPaxOptionDTO> paxOptionDTOs = paxOptions.stream()
                    .map(pax -> TourPaxOptionDTO.builder()
                            .id(pax.getId())
                            .minPax(pax.getMinPax())
                            .maxPax(pax.getMaxPax())
                            .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                            .build())
                    .collect(Collectors.toList());

            // Create a map to group services by category name
            Map<String, List<ServiceSummaryDTO>> servicesByCategoryName = new HashMap<>();

            for (TourDayService tds : allTourDayServices) {
                Service service = tds.getService();
                TourDay tourDay = tds.getTourDay();

                if (service != null && service.getServiceCategory() != null) {
                    String categoryName = service.getServiceCategory().getCategoryName();

                    // Determine service status
                    String status = determineServiceStatus(service.getStartDate(), service.getEndDate());

                    // Calculate pax prices for this service using TourPax information
                    Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();
                    for (TourPax pax : paxOptions) {
                        // Calculate price based on TourPax settings and service selling price
                        Double adjustedPrice = calculatePriceForPax(tds.getSellingPrice(), pax);

                        paxPrices.put(pax.getId().toString(), PaxPriceInfoDTO.builder()
                                .paxId(pax.getId())
                                .minPax(pax.getMinPax())
                                .maxPax(pax.getMaxPax())
                                .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                                .price(adjustedPrice)
                                .build());
                    }

                    ServiceSummaryDTO serviceSummary = ServiceSummaryDTO.builder()
                            .id(service.getId())
                            .name(service.getName())
                            .dayNumber(tourDay.getDayNumber())
                            .status(status)
                            .nettPrice(service.getNettPrice())
                            .sellingPrice(tds.getSellingPrice())
                            .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                            .locationId(tourDay.getLocation() != null ? tourDay.getLocation().getId() : null)
                            .serviceProviderName(service.getServiceProvider() != null ? service.getServiceProvider().getName() : null)
                            .serviceProviderId(service.getServiceProvider() != null ? service.getServiceProvider().getId() : null)
                            .paxPrices(paxPrices)
                            .build();

                    // Add to the category list by category name
                    if (!servicesByCategoryName.containsKey(categoryName)) {
                        servicesByCategoryName.put(categoryName, new ArrayList<>());
                    }
                    servicesByCategoryName.get(categoryName).add(serviceSummary);
                }
            }

            // Convert map to list of ServiceCategoryDTO
            List<TourServiceCategoryDTO> categoryDTOs = new ArrayList<>();
            for (Map.Entry<String, List<ServiceSummaryDTO>> entry : servicesByCategoryName.entrySet()) {
                TourServiceCategoryDTO categoryDTO = TourServiceCategoryDTO.builder()
                        .categoryName(entry.getKey())
                        .services(entry.getValue())
                        .build();
                categoryDTOs.add(categoryDTO);
            }

            // Build response
            TourServiceListDTO response = TourServiceListDTO.builder()
                    .tourId(tourId)
                    .tourName(tour.getName())
                    .serviceCategories(categoryDTOs)
                    //.paxOptions(paxOptionDTOs)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), SERVICES_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICES_LOAD_FAIL, ex);
        }
    }

    private Double calculatePriceForPax(Double basePrice, TourPax pax) {
        // Simple calculation example using TourPax information:
        double fixedCostPerPerson = pax.getFixedCost() / Math.max(pax.getMinPax(), 1);
        double extraCostPerPerson = pax.getExtraHotelCost() / Math.max(pax.getMinPax(), 1);

        // Apply tiered pricing based on pax range
        if (pax.getMinPax() <= 2) {
            // Higher price for 1-2 people
            return basePrice * 1.2 + fixedCostPerPerson + extraCostPerPerson;
        } else if (pax.getMinPax() <= 5) {
            // Standard price for 3-5 people
            return basePrice + fixedCostPerPerson + (extraCostPerPerson * 0.8); // 20% discount on extra costs
        } else {
            // Discount for 6+ people
            return basePrice * 0.9 + fixedCostPerPerson * 0.8; // 10% discount on base, 20% on fixed costs
        }
    }

    private String determineServiceStatus(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startDateTime == null || endDateTime == null) {
            return "UNKNOWN";
        }

        if (now.isBefore(startDateTime)) {
            return "UPCOMING";
        } else if (now.isAfter(endDateTime)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }
}
