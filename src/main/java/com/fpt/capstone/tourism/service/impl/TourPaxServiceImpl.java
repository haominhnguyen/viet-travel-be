package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.service.TourPaxService;

import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourPax;
import com.fpt.capstone.tourism.repository.TourPaxRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.service.TourPaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
public class TourPaxServiceImpl implements TourPaxService {

    private final TourRepository tourRepository;
    private final TourPaxRepository tourPaxRepository;


    @Autowired
    public TourPaxServiceImpl(TourRepository tourRepository, TourPaxRepository tourPaxRepository) {
        this.tourRepository = tourRepository;
        this.tourPaxRepository = tourPaxRepository;
    }

    @Override
    public GeneralResponse<List<TourPaxFullDTO>> getTourPaxConfigurations(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // Updated to use the repository method that filters by deleted=false
            List<TourPax> paxConfigurations = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tourId);
            Date now = new Date();

            List<TourPaxFullDTO> paxDTOs = paxConfigurations.stream()
                    .map(pax -> TourPaxFullDTO.builder()
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
                            .build())
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIGS_LOAD_SUCCESS, paxDTOs);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve pax configurations", ex);
        }
    }

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
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_LOAD_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve pax configuration", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourPaxFullDTO> createTourPaxConfiguration(Long tourId, TourPaxCreateRequestDTO request) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

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
                    .build();

            pax = tourPaxRepository.save(pax);

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
                    .build();

            return new GeneralResponse<>(HttpStatus.CREATED.value(), PAX_CONFIG_CREATE_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create pax configuration", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourPaxFullDTO> updateTourPaxConfiguration(Long tourId, Long paxId, TourPaxUpdateRequestDTO request) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            TourPax pax = tourPaxRepository.findById(paxId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND + " with id: " + paxId));

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

            // Update the pax configuration
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

            pax = tourPaxRepository.save(pax);

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
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_UPDATE_SUCCESS, paxDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update pax configuration", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<String> deleteTourPaxConfiguration(Long tourId, Long paxId) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            TourPax pax = tourPaxRepository.findById(paxId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND + " with id: " + paxId));

            if (!pax.getTour().getId().equals(tourId)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }
            pax.setDeleted(true);
            tourPaxRepository.save(pax);

            return new GeneralResponse<>(HttpStatus.OK.value(), PAX_CONFIG_DELETE_SUCCESS,
                    "Pax configuration with id " + paxId + " has been marked as deleted");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete pax configuration", ex);
        }
    }

    /**
     * Check if a pax configuration overlaps with existing configurations
     * Overlap occurs when:
     * 1. Pax ranges overlap (e.g. 1-3 and 2-5)
     * 2. Date ranges overlap (e.g. Jan 1 - Jan 10 and Jan 5 - Jan 15)
     */
    private boolean checkForOverlappingPaxConfigurations(Long tourId, Long excludePaxId,
                                                         int minPax, int maxPax, Date validFrom, Date validTo) {

        List<TourPax> existingConfigs = tourPaxRepository.findByTourIdOrderByMinPax(tourId);

        for (TourPax config : existingConfigs) {
            // Skip the config being updated
            if (excludePaxId != null && config.getId().equals(excludePaxId)) {
                continue;
            }

            // Check for pax range overlap
            boolean paxOverlap = !(maxPax < config.getMinPax() || minPax > config.getMaxPax());

            // Check for date range overlap
            boolean dateOverlap = !(validTo.before(config.getValidFrom()) || validFrom.after(config.getValidTo()));

            // If both overlap, we have a conflict
            if (paxOverlap && dateOverlap) {
                return true;
            }
        }

        return false;
    }
}
