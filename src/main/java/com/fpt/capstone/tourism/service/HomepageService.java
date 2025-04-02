package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface HomepageService {
    GeneralResponse<HomepageDTO> viewHomepage(int numberTour, int numberBlog, int numberActivity, int numberLocation);


    GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> viewAllHotel(int page, int size, String keyword, Integer star);

//    GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllRestaurant(int page, int size, String keyword);

    GeneralResponse<PagingDTO<List<PublicTourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId, String sortByPrice);

//    GeneralResponse<PublicActivityDetailDTO> viewPublicActivityDetail(Long id, int numberActivity);

    GeneralResponse<PublicTourDetailDTO> viewTourDetail(Long id);

    GeneralResponse<PublicLocationDetailDTO> viewPublicLocationDetail(Long id);

    GeneralResponse<PublicHotelDetailDTO> viewPublicHotelDetail(Long serviceProviderId);

    GeneralResponse<?> search(String keyword);

    GeneralResponse<?> getListLocation();

}
