package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourPaxDTO;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;

import java.util.List;

public interface TourPaxService {
    GeneralResponse<List<TourPaxFullDTO>> getTourPaxConfigurations(Long tourId);

    GeneralResponse<TourPaxFullDTO> getTourPaxConfiguration(Long tourId, Long paxId);

    GeneralResponse<TourPaxFullDTO> createTourPaxConfiguration(Long tourId, TourPaxCreateRequestDTO request);

    GeneralResponse<TourPaxFullDTO> updateTourPaxConfiguration(Long tourId, Long paxId, TourPaxUpdateRequestDTO request);

    GeneralResponse<String> deleteTourPaxConfiguration(Long tourId, Long paxId);
}
