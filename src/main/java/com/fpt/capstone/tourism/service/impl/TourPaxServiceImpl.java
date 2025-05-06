package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayServicePricingDTO;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.dto.request.ServicePricingRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourPaxService;

import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;
import com.fpt.capstone.tourism.service.TourPaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class TourPaxServiceImpl implements TourPaxService {

    private final TourRepository tourRepository;
    private final TourPaxRepository tourPaxRepository;
    private final TourDayRepository tourDayRepository;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final ServicePaxPricingRepository servicePaxPricingRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    public GeneralResponse<TourPaxFullDTO> getTourPaxConfiguration(Long tourId, Long paxId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            TourPax pax = tourPaxRepository.findById(paxId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND + " with id: " + paxId));

            if (!pax.getTour().getId().equals(tourId)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }

            // Get all tour days for this tour
            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);

            // Get all tour day services
            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            // Get all non-deleted service-pax associations for this pax
            List<ServicePaxPricing> paxServicePricings = servicePaxPricingRepository.findByTourPaxIdAndDeletedFalse(paxId);

            // Create a map of service ID to pricing for quick lookup
            Map<Long, ServicePaxPricing> servicePricingMap = paxServicePricings.stream()
                    .collect(Collectors.toMap(
                            spp -> spp.getTourDayService().getId(),
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            // Create DTOs for the service associations
            List<TourDayServicePricingDTO> serviceAssociations = allTourDayServices.stream()
                    .map(tds -> {
                        Service service = tds.getService();
                        ServicePaxPricing pricing = servicePricingMap.get(tds.getId());
                        return TourDayServicePricingDTO.builder()
                                .tourDayServiceId(tds.getId())
                                .serviceId(service.getId())
                                .serviceName(service.getName())
                                .dayNumber(tds.getTourDay().getDayNumber())
                                .categoryName(service.getServiceCategory() != null ?
                                        service.getServiceCategory().getCategoryName() : null)
                                .defaultServicePrice(service.getNettPrice())
                                .isAssociated(pricing != null)
                                .build();
                    })
                    .collect(Collectors.toList());

            Date now = new Date();
            TourPaxFullDTO paxDTO = TourPaxFullDTO.builder()
                    .id(pax.getId())
                    .tourId(pax.getTour().getId())
                    .minPax(pax.getMinPax())
                    .maxPax(pax.getMaxPax())
                    .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                    .fixedCost(pax.getFixedCost())
                    .extraHotelCost(pax.getExtraHotelCost())
                    .nettPricePerPax(pax.getNettPricePerPax())
                    .sellingPrice(pax.getSellingPrice())
                    .validFrom(pax.getValidFrom())
                    .validTo(pax.getValidTo())
                    .isValid(now.after(pax.getValidFrom()) && now.before(pax.getValidTo()))
                    .serviceAssociations(serviceAssociations)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_LOAD_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_TO_RETRIEVE_PAX_CONFIGURATION, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourPaxFullDTO> createTourPaxConfiguration(Long tourId, TourPaxCreateRequestDTO request) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " id: " + tourId));

            // Validate pax range
            if (request.getMinPax() > request.getMaxPax()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_INVALID_RANGE);
            }

            // Validate dates
            if (request.getValidFrom().after(request.getValidTo())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_INVALID_DATES);
            }

            // Check for overlapping pax ranges and date ranges
            boolean overlaps = checkForOverlappingPaxConfigurations(tourId, null, request.getMinPax(), request.getMaxPax(),
                    request.getValidFrom(), request.getValidTo());

            if (overlaps) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_OVERLAP);
            }

            TourPax pax = TourPax.builder()
                    .tour(tour)
                    .minPax(request.getMinPax())
                    .maxPax(request.getMaxPax())
                    .fixedCost(request.getFixedCost())
                    .extraHotelCost(request.getExtraHotelCost())
                    .nettPricePerPax(request.getNettPricePerPax())
                    .sellingPrice(request.getSellingPrice())
                    .validFrom(request.getValidFrom())
                    .validTo(request.getValidTo())
                    .deleted(false)
                    .build();

            pax = tourPaxRepository.save(pax);

            // Get all tour days for this tour
            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);

            // Get all tour day services
            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            // Create service-pax associations for each tour day service
            List<ServicePaxPricing> servicePaxPricings = new ArrayList<>();
            List<TourDayServicePricingDTO> serviceAssociations = new ArrayList<>();

            for (TourDayService tds : allTourDayServices) {
                Service service = tds.getService();

                // Create default association
                ServicePaxPricing servicePaxPricing = ServicePaxPricing.builder()
                        .tourDayService(tds)
                        .tourPax(pax)
                        .deleted(false)
                        .build();

                servicePaxPricing = servicePaxPricingRepository.save(servicePaxPricing);
                servicePaxPricings.add(servicePaxPricing);

                // Create DTO for response
                serviceAssociations.add(TourDayServicePricingDTO.builder()
                        .tourDayServiceId(tds.getId())
                        .serviceId(service.getId())
                        .serviceName(service.getName())
                        .dayNumber(tds.getTourDay().getDayNumber())
                        .categoryName(service.getServiceCategory() != null ?
                                service.getServiceCategory().getCategoryName() : null)
                        .defaultServicePrice(service.getNettPrice())
                        .isAssociated(true)
                        .build());
            }

            Date now = new Date();
            TourPaxFullDTO paxDTO = TourPaxFullDTO.builder()
                    .id(pax.getId())
                    .tourId(pax.getTour().getId())
                    .minPax(pax.getMinPax())
                    .maxPax(pax.getMaxPax())
                    .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                    .fixedCost(pax.getFixedCost())
                    .extraHotelCost(pax.getExtraHotelCost())
                    .nettPricePerPax(pax.getNettPricePerPax())
                    .sellingPrice(pax.getSellingPrice())
                    .validFrom(pax.getValidFrom())
                    .validTo(pax.getValidTo())
                    .isValid(now.after(pax.getValidFrom()) && now.before(pax.getValidTo()))
                    .serviceAssociations(serviceAssociations)
                    .isDeleted(false)
                    .build();

            return new GeneralResponse<>(HttpStatus.CREATED.value(), PAX_CONFIG_CREATE_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_TO_CREATE_PAX_CONFIGURATION, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourPaxFullDTO> updateTourPaxConfiguration(Long tourId, Long paxId, TourPaxUpdateRequestDTO request) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " id: " + tourId));

            if (TourType.SIC.equals(tour.getTourType())) {
                boolean hasActiveBookings = tourBookingRepository.existsByTourIdAndStatusIn(
                        tour.getId(),
                        List.of(TourBookingStatus.SUCCESS, TourBookingStatus.PENDING)
                );

                if (hasActiveBookings) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            "Không thể cập nhật giá pax của tour SIC khi đã có đơn đặt tour với trạng thái Đang Chờ hoặc Đã Thành Công");
                }
            }

            TourPax pax = tourPaxRepository.findById(paxId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND + " id: " + paxId));

            if (!pax.getTour().getId().equals(tourId)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }

            // Set new values if provided
            Integer minPax = request.getMinPax() != null ? request.getMinPax() : pax.getMinPax();
            Integer maxPax = request.getMaxPax() != null ? request.getMaxPax() : pax.getMaxPax();
            Date validFrom = request.getValidFrom() != null ? request.getValidFrom() : pax.getValidFrom();
            Date validTo = request.getValidTo() != null ? request.getValidTo() : pax.getValidTo();

            // Validate pax range
            if (minPax > maxPax) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_INVALID_RANGE);
            }

            // Validate dates
            if (validFrom.after(validTo)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_INVALID_DATES);
            }

            // Check for overlapping pax ranges and date ranges if min/max/dates changed
            if (request.getMinPax() != null || request.getMaxPax() != null ||
                    request.getValidFrom() != null || request.getValidTo() != null) {

                boolean overlaps = checkForOverlappingPaxConfigurations(tourId, paxId, minPax, maxPax, validFrom, validTo);

                if (overlaps) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_OVERLAP);
                }
            }

            pax.setMinPax(minPax);
            pax.setMaxPax(maxPax);

            if (request.getFixedCost() != null) {
                pax.setFixedCost(request.getFixedCost());
            }

            if (request.getExtraHotelCost() != null) {
                pax.setExtraHotelCost(request.getExtraHotelCost());
            }

            if (request.getNettPricePerPax() != null) {
                pax.setNettPricePerPax(request.getNettPricePerPax());
            }

            if (request.getSellingPrice() != null) {
                pax.setSellingPrice(request.getSellingPrice());
            }

            pax.setValidFrom(validFrom);
            pax.setValidTo(validTo);
            pax.setDeleted(false);

            pax = tourPaxRepository.save(pax);

            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);

            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            List<ServicePaxPricing> existingPricings = servicePaxPricingRepository.findByTourPaxId(paxId);

            // Create a map for quick lookup
            Map<Long, ServicePaxPricing> existingPricingsMap = existingPricings.stream()
                    .collect(Collectors.toMap(
                            spp -> spp.getTourDayService().getId(),
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            // Process service pricings from the request
            Map<Long, Double> requestPricingsMap = new HashMap<>();
            if (request.getServicePricings() != null) {
                for (ServicePricingRequestDTO pricingRequest : request.getServicePricings()) {
                    requestPricingsMap.put(pricingRequest.getTourDayServiceId(), pricingRequest.getCustomServicePrice());
                }
            }

            // Update or create service-pax associations
            List<TourDayServicePricingDTO> serviceAssociations = new ArrayList<>();

            for (TourDayService tds : allTourDayServices) {
                Service service = tds.getService();
                Long tdsId = tds.getId();

                ServicePaxPricing pricing = existingPricingsMap.get(tdsId);

                if (pricing == null) {
                    pricing = ServicePaxPricing.builder()
                            .tourDayService(tds)
                            .tourPax(pax)
                            .deleted(false)
                            .build();
                } else {
                    pricing.setDeleted(false);
                }
                pricing = servicePaxPricingRepository.save(pricing);

                serviceAssociations.add(TourDayServicePricingDTO.builder()
                        .tourDayServiceId(tdsId)
                        .serviceId(service.getId())
                        .serviceName(service.getName())
                        .dayNumber(tds.getTourDay().getDayNumber())
                        .categoryName(service.getServiceCategory() != null ?
                                service.getServiceCategory().getCategoryName() : null)
                        .defaultServicePrice(service.getNettPrice())
                        .isAssociated(true)
                        .build());
            }

            Date now = new Date();
            TourPaxFullDTO paxDTO = TourPaxFullDTO.builder()
                    .id(pax.getId())
                    .tourId(pax.getTour().getId())
                    .minPax(pax.getMinPax())
                    .maxPax(pax.getMaxPax())
                    .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                    .fixedCost(pax.getFixedCost())
                    .extraHotelCost(pax.getExtraHotelCost())
                    .nettPricePerPax(pax.getNettPricePerPax())
                    .sellingPrice(pax.getSellingPrice())
                    .validFrom(pax.getValidFrom())
                    .validTo(pax.getValidTo())
                    .isValid(now.after(pax.getValidFrom()) && now.before(pax.getValidTo()))
                    .serviceAssociations(serviceAssociations)
                    .isDeleted(false)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_UPDATE_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_PAX_CONFIGURATION, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<String> deleteTourPaxConfiguration(Long tourId, Long paxId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " id: " + tourId));

            if (TourType.SIC.equals(tour.getTourType())) {
                boolean hasActiveBookings = tourBookingRepository.existsByTourIdAndStatusIn(
                        tour.getId(),
                        List.of(TourBookingStatus.SUCCESS, TourBookingStatus.PENDING)
                );

                if (hasActiveBookings) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            "Không thể xóa giá pax của tour SIC khi đã có đơn đặt tour với trạng thái Đang Chờ hoặc Đã Thành Công");
                }
            }

            TourPax pax = tourPaxRepository.findById(paxId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND + " id: " + paxId));

            if (!pax.getTour().getId().equals(tourId)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }

            // Get all service-pax associations for this pax
            List<ServicePaxPricing> paxServicePricings = servicePaxPricingRepository.findByTourPaxId(paxId);

            // Mark all associations as deleted
            for (ServicePaxPricing pricing : paxServicePricings) {
                pricing.setDeleted(true);
                servicePaxPricingRepository.save(pricing);
            }

            // Mark the pax as deleted
            pax.setDeleted(true);
            tourPaxRepository.save(pax);

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_DELETE_SUCCESS,
                    "Cấu hình pax với id " + paxId + " và tất cả các liên kết dịch vụ của nó đã được đánh dấu là đã xóa.");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_TO_DELETE_PAX_CONFIGURATION, ex);
        }
    }

    @Override
    public GeneralResponse<List<TourPaxFullDTO>> getTourPaxConfigurations(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " id: " + tourId));

            List<TourPax> paxConfigurations = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tourId);

            // Get all tour days for this tour
            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);

            // Get all tour day services
            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            // Get all pax IDs
            List<Long> paxIds = paxConfigurations.stream()
                    .map(TourPax::getId)
                    .collect(Collectors.toList());

            // Get all non-deleted service-pax associations for these paxes
            List<ServicePaxPricing> allPaxServicePricings = servicePaxPricingRepository.findByTourPaxIdInAndDeletedFalse(paxIds);

            // Group by paxId for quick lookup
            Map<Long, List<ServicePaxPricing>> paxToPricingsMap = allPaxServicePricings.stream()
                    .collect(Collectors.groupingBy(pricing -> pricing.getTourPax().getId()));

            Date now = new Date();

            List<TourPaxFullDTO> paxDTOs = new ArrayList<>();

            for (TourPax pax : paxConfigurations) {
                Long paxId = pax.getId();

                // Get pricings for this pax
                List<ServicePaxPricing> paxPricings = paxToPricingsMap.getOrDefault(paxId, new ArrayList<>());

                // Create a map for quick lookup of service to pricing
                Map<Long, ServicePaxPricing> serviceToPricingMap = paxPricings.stream()
                        .collect(Collectors.toMap(
                                pricing -> pricing.getTourDayService().getId(),
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

                List<TourDayServicePricingDTO> serviceAssociations = new ArrayList<>();

                for (TourDayService tds : allTourDayServices) {
                    Service service = tds.getService();
                    ServicePaxPricing pricing = serviceToPricingMap.get(tds.getId());

                    serviceAssociations.add(TourDayServicePricingDTO.builder()
                            .tourDayServiceId(tds.getId())
                            .serviceId(service.getId())
                            .serviceName(service.getName())
                            .dayNumber(tds.getTourDay().getDayNumber())
                            .categoryName(service.getServiceCategory() != null ?
                                    service.getServiceCategory().getCategoryName() : null)
                            .defaultServicePrice(service.getNettPrice())
                            .isAssociated(pricing != null)
                            .build());
                }

                TourPaxFullDTO paxDTO = TourPaxFullDTO.builder()
                        .id(pax.getId())
                        .tourId(pax.getTour().getId())
                        .minPax(pax.getMinPax())
                        .maxPax(pax.getMaxPax())
                        .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                        .fixedCost(pax.getFixedCost())
                        .extraHotelCost(pax.getExtraHotelCost())
                        .nettPricePerPax(pax.getNettPricePerPax())
                        .sellingPrice(pax.getSellingPrice())
                        .validFrom(pax.getValidFrom())
                        .validTo(pax.getValidTo())
                        .isValid(now.after(pax.getValidFrom()) && now.before(pax.getValidTo()))
                        .serviceAssociations(serviceAssociations)
                        .serviceAssociationCount(paxPricings.size())
                        .build();
                paxDTOs.add(paxDTO);
            }
            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_LOAD_SUCCESS, paxDTOs);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_TO_RETRIEVE_PAX_CONFIGURATION, ex);
        }
    }
    /**
     * Check if a pax configuration overlaps with existing configurations
     * Overlap occurs when:
     * 1. Pax ranges overlap (e.g. 1-3 and 2-5)
     * 2. Date ranges overlap (e.g. Jan 1 - Jan 10 and Jan 5 - Jan 15)
     */
    private boolean checkForOverlappingPaxConfigurations(Long tourId, Long paxIdToExclude,
                                                         Integer minPax, Integer maxPax,
                                                         Date validFrom, Date validTo) {
        // Get all non-deleted pax configurations for this tour, excluding the one being updated if applicable
        List<TourPax> existingConfigs = paxIdToExclude == null ?
                tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tourId) :
                tourPaxRepository.findByTourIdAndIdNotAndDeletedFalseOrderByMinPax(tourId, paxIdToExclude);
        // Check for overlaps in both pax range and date range
        for (TourPax config : existingConfigs) {
            // Check if pax ranges overlap
            boolean paxRangeOverlaps = (minPax <= config.getMaxPax() && maxPax >= config.getMinPax());
            // Check if date ranges overlap
            boolean dateRangeOverlaps = (validFrom.before(config.getValidTo()) && validTo.after(config.getValidFrom()));
            // If both overlap, then there's a conflict
            if (paxRangeOverlaps && dateRangeOverlaps) {
                return true;
            }
        }
        return false;
    }
}
