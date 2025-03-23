package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBasicDTO;
import com.fpt.capstone.tourism.dto.common.TourDetailDTO;
import com.fpt.capstone.tourism.dto.request.TourRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
import com.fpt.capstone.tourism.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface TourService {
    PublicTourDTO findTopTourOfYear();

    List<PublicTourDTO> findTrendingTours(int numberTour);

    GeneralResponse<PagingDTO<List<PublicTourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId, String sortByPrice);

    List<PublicTourDTO> findSameLocationPublicTour(List<Long> locationIds);


    GeneralResponse<PagingDTO<List<TourBasicDTO>>> getAllTours(String keyword, Boolean isDeleted, Boolean isOpened, Pageable pageable);

    GeneralResponse<TourDetailDTO> getTourDetail(Long id);

    @Transactional
    GeneralResponse<TourResponseDTO> createTour(TourRequestDTO tourRequestDTO, User currentUser);

    @Transactional
    GeneralResponse<TourResponseDTO> updateTour(Long id, TourRequestDTO tourRequestDTO, User currentUser);


}
