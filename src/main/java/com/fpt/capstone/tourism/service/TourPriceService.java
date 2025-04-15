package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourPriceConfigRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceConfigResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceListResponseDTO;
import com.fpt.capstone.tourism.model.User;

public interface TourPriceService {
    GeneralResponse<TourPriceListResponseDTO> getTourPriceConfigurations(Long tourId);
    GeneralResponse<TourPriceConfigResponseDTO> getTourPriceConfigurationById(Long tourId, Long configId);
    GeneralResponse<String> deleteTourPriceConfiguration(Long tourId, Long configId);

    GeneralResponse<TourPriceConfigResponseDTO> updateTourPrice(TourPriceConfigRequestDTO requestDTO, User user);
}