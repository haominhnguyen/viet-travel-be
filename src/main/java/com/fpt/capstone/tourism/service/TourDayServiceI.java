package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateRequestDTO;

import java.util.List;

public interface TourDayServiceI {
    GeneralResponse<List<TourDayFullDTO>> getTourDayDetail(Long tourId);
    GeneralResponse<TourDayFullDTO> createTourDay(TourDayCreateRequestDTO createRequestDTO);
    GeneralResponse<TourDayFullDTO> updateTourDay(Long tourDayId, TourDayUpdateRequestDTO tourDayUpdateDTO);

}
