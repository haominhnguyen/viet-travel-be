package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourDayServiceResponseDTO;
import com.fpt.capstone.tourism.model.User;
import jakarta.validation.Valid;

import java.util.List;


public interface TourDayServiceI {
    GeneralResponse<List<TourDayFullDTO>> getTourDayDetail(Long tourId, Boolean isDeleted);

    GeneralResponse<TourDayFullDTO> createTourDay(Long tourId, TourDayCreateRequestDTO request);

    GeneralResponse<TourDayFullDTO> updateTourDay(Long id, Long tourId, TourDayUpdateRequestDTO request);

    GeneralResponse<String> changeTourDayStatus(Long id, Long tourId, Boolean isDeleted);
}
