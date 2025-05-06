package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourPriceConfigRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceConfigResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceListResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.ServicePaxPricing;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourPax;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.ServicePaxPricingRepository;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TourPaxRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.service.TourPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class TourPriceServiceImpl implements TourPriceService {
    private final TourRepository tourRepository;
    private final TourPaxRepository tourPaxRepository;
    private final ServicePaxPricingRepository servicePaxPricingRepository;
    private final TourBookingRepository tourBookingRepository;

    @Override
    public GeneralResponse<TourPriceListResponseDTO> getTourPriceConfigurations(Long tourId) {
        try {
            // Validate Tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get all non-deleted price configurations for this tour
            List<TourPax> tourPaxList = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tourId);

            // Build response
            List<TourPriceConfigResponseDTO> configDTOs = tourPaxList.stream()
                    .map(this::buildResponseDTO)
                    .collect(Collectors.toList());

            TourPriceListResponseDTO responseDTO = TourPriceListResponseDTO.builder()
                    .tourId(tourId)
                    .tourName(tour.getName())
                    .priceConfigurations(configDTOs)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), CONFIGS_RETRIEVED, responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lấy cấu hình giá tour: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GeneralResponse<TourPriceConfigResponseDTO> getTourPriceConfigurationById(Long tourId, Long configId) {
        try {
            // Validate Tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get the price configuration
            TourPax tourPax = tourPaxRepository.findById(configId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND));

            // Verify it belongs to the specified tour and is not deleted
            if (!tourPax.getTour().getId().equals(tourId) || tourPax.getDeleted()) {
                throw BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND);
            }

            // Build response
            TourPriceConfigResponseDTO responseDTO = buildResponseDTO(tourPax);

            return new GeneralResponse<>(HttpStatus.OK.value(), CONFIGS_RETRIEVED, responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lấy cấu hình giá tour: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<String> deleteTourPriceConfiguration(Long tourId, Long configId) {
        try {
            // Validate Tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get the price configuration
            TourPax tourPax = tourPaxRepository.findById(configId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND));

            // Verify it belongs to the specified tour
            if (!tourPax.getTour().getId().equals(tourId)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }

            // Get all service-pax associations for this pax
            List<ServicePaxPricing> paxServicePricings = servicePaxPricingRepository.findByTourPaxId(configId);

            // Mark all associations as deleted
            for (ServicePaxPricing pricing : paxServicePricings) {
                pricing.setDeleted(true);
                servicePaxPricingRepository.save(pricing);
            }

            // Mark the pax configuration as deleted
            tourPax.setDeleted(true);
            tourPaxRepository.save(tourPax);

            return new GeneralResponse<>(HttpStatus.OK.value(), CONFIG_DELETED,
                    "Cấu hình giá với id " + configId + " và tất cả các liên kết dịch vụ của nó đã được đánh dấu là đã xóa");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xóa cấu hình giá: " + ex.getMessage(), ex);
        }
    }


//    @Override
//    @Transactional
//    public GeneralResponse<TourPriceConfigResponseDTO> updateTourPrice(TourPriceConfigRequestDTO requestDTO, User user) {
//        try {
//            // Validate Tour exists
//            Tour tour = tourRepository.findById(requestDTO.getTourId())
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
//
//            // Get existing configuration
//            TourPax tourPax = tourPaxRepository.findById(requestDTO.getId())
//                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND));
//
//            // Verify it belongs to the specified tour
//            if (!tourPax.getTour().getId().equals(requestDTO.getTourId())) {
//                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
//            }
//
//            // Validate date range
//            if (requestDTO.getValidTo().before(requestDTO.getValidFrom())) {
//                throw BusinessException.of(HttpStatus.BAD_REQUEST, DATE_RANGE_INVALID);
//            }
//
//            // Update all price-related fields
//            tourPax.setSellingPrice(requestDTO.getSellingPrice());
//            tourPax.setFixedCost(requestDTO.getFixedCost());
//            tourPax.setExtraHotelCost(requestDTO.getExtraHotelCost());
//            tourPax.setNettPricePerPax(requestDTO.getNettPricePerPax());
//            tourPax.setValidFrom(requestDTO.getValidFrom());
//            tourPax.setValidTo(requestDTO.getValidTo());
//
//            // Save updated configuration
//            tourPax = tourPaxRepository.save(tourPax);
//            TourPriceConfigResponseDTO responseDTO = buildResponseDTO(tourPax);
//            return new GeneralResponse<>(HttpStatus.OK.value(), CONFIG_UPDATED, responseDTO);
//        } catch (BusinessException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Không thể cập nhật giá tour: " + ex.getMessage(), ex);
//        }
//    }

    @Override
    @Transactional
    public GeneralResponse<TourPriceConfigResponseDTO> updateTourPrice(TourPriceConfigRequestDTO requestDTO, User user) {
        try {
            Tour tour = tourRepository.findById(requestDTO.getTourId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            if (TourType.SIC.equals(tour.getTourType())) {
                boolean hasActiveBookings = tourBookingRepository.existsByTourIdAndStatusIn(
                        tour.getId(),
                        List.of(TourBookingStatus.SUCCESS, TourBookingStatus.PENDING)
                );

                if (hasActiveBookings) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST,
                            "Không thể cập nhật giá tour SIC khi đã có đơn đặt tour với trạng thái Đang Chờ hoặc Đã Thành Công");
                }
            }
            TourPax tourPax = tourPaxRepository.findById(requestDTO.getId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, PAX_CONFIG_NOT_FOUND));

            if (!tourPax.getTour().getId().equals(requestDTO.getTourId())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, PAX_CONFIG_NOT_ASSOCIATED);
            }

            if (requestDTO.getValidTo().before(requestDTO.getValidFrom())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, DATE_RANGE_INVALID);
            }
            tourPax.setSellingPrice(requestDTO.getSellingPrice());
            tourPax.setFixedCost(requestDTO.getFixedCost());
            tourPax.setExtraHotelCost(requestDTO.getExtraHotelCost());
            tourPax.setNettPricePerPax(requestDTO.getNettPricePerPax());
            tourPax.setValidFrom(requestDTO.getValidFrom());
            tourPax.setValidTo(requestDTO.getValidTo());

            tourPax = tourPaxRepository.save(tourPax);
            TourPriceConfigResponseDTO responseDTO = buildResponseDTO(tourPax);
            return new GeneralResponse<>(HttpStatus.OK.value(), CONFIG_UPDATED, responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể cập nhật giá tour: " + ex.getMessage(), ex);
        }
    }

    private TourPriceConfigResponseDTO buildResponseDTO(TourPax tourPax) {
        return TourPriceConfigResponseDTO.builder()
                .id(tourPax.getId())
                .tourId(tourPax.getTour().getId())
                .tourName(tourPax.getTour().getName())
                .minPax(tourPax.getMinPax())
                .maxPax(tourPax.getMaxPax())
                .paxRange(tourPax.getMinPax() + "-" + tourPax.getMaxPax())
                .nettPricePerPax(tourPax.getNettPricePerPax())
                .sellingPrice(tourPax.getSellingPrice())
                .fixedCost(tourPax.getFixedCost())
                .extraHotelCost(tourPax.getExtraHotelCost())
                .validFrom(tourPax.getValidFrom())
                .validTo(tourPax.getValidTo())
                .createdAt(tourPax.getCreatedAt())
                .updatedAt(tourPax.getUpdatedAt())
                .build();
    }
}
