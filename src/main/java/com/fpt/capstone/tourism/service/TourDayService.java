package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;

import java.util.List;

public interface TourDayService {
    GeneralResponse<List<TourDayFullDTO>> getTourDayDetail(Long tourId);
}
