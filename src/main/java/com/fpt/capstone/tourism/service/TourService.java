package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.Date;
import java.util.List;

public interface TourService {
    TourDTO findTopTourOfYear();

    List<TourDTO> findTrendingTours(int numberTour);


    GeneralResponse<PagingDTO<List<TourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate);
}
