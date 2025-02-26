package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDetailDTO;

import java.util.Date;
import java.util.List;

public interface HomepageService {
    GeneralResponse<HomepageDTO> viewHomepage(int numberTour, int numberBlog, int numberActivity, int numberLocation);


    GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllHotel(int page, int size, String keyword);

//    GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllRestaurant(int page, int size, String keyword);

    GeneralResponse<PagingDTO<List<TourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate);

//    GeneralResponse<PublicActivityDetailDTO> viewPublicActivityDetail(Long id, int numberActivity);

    GeneralResponse<PublicTourDetailDTO> viewTourDetail(Long id);
}
