package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;

import java.util.Date;
import java.util.List;

public interface TourService {
    PublicTourDTO findTopTourOfYear();

    List<PublicTourDTO> findTrendingTours(int numberTour);


    GeneralResponse<PagingDTO<List<PublicTourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate);

    List<PublicTourDTO> findSameLocationPublicTour(List<Long> locationIds);
}
