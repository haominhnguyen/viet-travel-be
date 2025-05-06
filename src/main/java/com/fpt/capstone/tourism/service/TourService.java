package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.TourRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.TourMarkupResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.TourStatus;
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

    GeneralResponse<TourMarkupResponseDTO> getTourMarkupPercentage(Long tourId);
    GeneralResponse<TourResponseDTO> updateTourMarkupPercentage(Long tourId, Double markUpPercent);

    GeneralResponse<TourDetailDTO> getTourWithActiveSchedule(Long id);

    GeneralResponse<PagingDTO<List<TourProcessDTO>>> getAllTourNeedToProcess(int page, int size, String keyword, TourStatus tourStatus, String orderDate);

    GeneralResponse<?> getDetailTourNeedToProcess(Long tourId);

    GeneralResponse<?> getDetailTourDay(Long tourId, Long tourDayId);

    GeneralResponse<?> approveTourProcess(Long tourId);

    GeneralResponse<?> rejectTourProcess(Long tourId);

    GeneralResponse<?> viewDashboard(LocalDate fromDate,LocalDate toDate);

    GeneralResponse<TourResponseDTO> sendTourForApproval(Long tourId, User user);

    GeneralResponse<TourResponseDTO> openTour(Long tourId, User user);

    GeneralResponse<TourResponseDTO> changeToPendingPricing(Long tourId, User user);
}
